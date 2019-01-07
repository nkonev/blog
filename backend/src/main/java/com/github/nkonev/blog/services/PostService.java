package com.github.nkonev.blog.services;

import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.converter.PostConverter;
import com.github.nkonev.blog.dto.*;
import com.github.nkonev.blog.entity.elasticsearch.IndexPost;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.entity.jpa.UserRole;
import com.github.nkonev.blog.exception.BadRequestException;
import com.github.nkonev.blog.exception.DataNotFoundException;
import com.github.nkonev.blog.repo.elasticsearch.IndexPostRepository;
import com.github.nkonev.blog.repo.jpa.CommentRepository;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import com.github.nkonev.blog.utils.PageUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import javax.validation.constraints.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.nkonev.blog.entity.elasticsearch.IndexPost.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class PostService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PostConverter postConverter;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private IndexPostRepository indexPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private SeoCacheListenerProxy seoCacheListenerProxy;

    @Autowired
    private SeoCacheService seoCacheService;

    @Autowired
    private RoleHierarchy roleHierarchy;

    @Value("${custom.elasticsearch.get.all.index.ids.chunk.size:20}")
    private int chunkSize;

    @Value("${custom.elasticsearch.search.field.text.slop:24}")
    private int searchFieldTextSlop;

    @Value("${custom.elasticsearch.search.field.title.slop:8}")
    private int searchFieldTitleSlop;

    @Value("${custom.elasticsearch.highlight.field.text.num.of.fragments:5}")
    private int highlightFieldTextNumOfFragments;

    @Value("${custom.elasticsearch.highlight.field.title.num.of.fragments:1}")
    private int highlightFieldTitleNumOfFragments;

    @Value("${custom.elasticsearch.highlight.field.text.num.of.fragments:150}")
    private int highlightFieldTextFragmentSize;

    @Value("${custom.elasticsearch.highlight.field.title.num.of.fragments:150}")
    private int highlightFieldTitleFragmentSize;

    @Value(Constants.ELASTICSEARCH_REFRESH_ON_START_KEY_TIMEOUT)
    private int timeout;

    @Value(Constants.ELASTICSEARCH_REFRESH_ON_START_KEY_TIMEUNIT)
    private TimeUnit timeUnit;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(PostService.class);

    private static class PostRowMapper implements RowMapper<PostDTO> {

        private boolean setTitle, setPost;

        public String getBaseSql() {
            return "select " +
                    "p.id, " +
                    "p.title_img, " +
                    "p.draft, " +
                    (setTitle ? "p.title, " : "") +
                    (setPost ? "p.text, "  : "") +
                    "p.create_date_time," +
                    "p.edit_date_time," +
                    "u.id as owner_id," +
                    "u.username as owner_login," +
                    "u.avatar as owner_avatar, " +
                    "(select count(*) from posts.comment c where c.post_id = p.id) as comment_count " +
                    "  from posts.post p " +
                    "    join auth.users u on p.owner_id = u.id ";
        }

        public PostRowMapper(boolean setTitle, boolean setPost) {
            this.setTitle = setTitle;
            this.setPost = setPost;
        }

        @Override
        public PostDTO mapRow(ResultSet resultSet, int i) throws SQLException {
            return new PostDTO(
                    resultSet.getLong("id"),
                    setTitle ? resultSet.getString("title") : null,
                    setPost ? resultSet.getString("text") : null,
                    resultSet.getString("title_img"),
                    resultSet.getObject("create_date_time", LocalDateTime.class),
                    resultSet.getObject("edit_date_time", LocalDateTime.class),
                    resultSet.getInt("comment_count"),
                    new UserAccountDTO(
                            resultSet.getLong("owner_id"),
                            resultSet.getString("owner_login"),
                            resultSet.getString("owner_avatar"),
                            null,
                            null
                    ),
                    resultSet.getBoolean("draft")
            );
        }
    }

    private final PostRowMapper rowMapperWithoutTextTitle = new PostRowMapper(false, false);
    private final PostRowMapper rowMapper = new PostRowMapper(true, true);

    private final SearchResultMapper searchResultMapper = new SearchResultMapper() {
        private String getHighlightedOrOriginalField(SearchHit searchHit, String fieldName){
            String field = (String) searchHit.getSourceAsMap().get(fieldName);;
            HighlightField highlightedField = searchHit.getHighlightFields().get(fieldName);
            if (highlightedField!=null && highlightedField.getFragments()!=null && highlightedField.getFragments().length>0){
                field = Arrays.stream(highlightedField.getFragments()).map(Text::toString).collect(Collectors.joining("... "));
            }
            return field;
        }

        @Override
        public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
            List<IndexPost> list = new ArrayList<>();
            for (SearchHit searchHit : response.getHits()) {
                if (response.getHits().getHits().length <= 0) {
                    return new AggregatedPageImpl<T>((List<T>) list);
                }
                IndexPost tempPost = new IndexPost();
                tempPost.setId(Long.valueOf(searchHit.getId()));
                tempPost.setTitle(getHighlightedOrOriginalField(searchHit, FIELD_TITLE));
                tempPost.setText(getHighlightedOrOriginalField(searchHit, FIELD_TEXT));
                tempPost.setDraft((boolean) searchHit.getSourceAsMap().get(FIELD_DRAFT));
                tempPost.setOwner((int) searchHit.getSourceAsMap().get(FIELD_OWNER));
                list.add(tempPost);
            }
            return new AggregatedPageImpl<T>((List<T>) list);
        }
    };

    public PostDTOWithAuthorization addPost(UserAccountDetailsDTO userAccount, @NotNull PostDTO postDTO){
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        if (postDTO.getId() != 0) {
            throw new BadRequestException("id cannot be set");
        }
        Post fromWeb = postConverter.convertToPost(postDTO, null);
        UserAccount ua = userAccountRepository.findById(userAccount.getId()).orElseThrow(()->new IllegalArgumentException("User account not found")); // Hibernate caches it
        fromWeb.setOwner(ua);
        Post saved = postRepository.saveAndFlush(fromWeb);
        indexPostRepository.save(postConverter.toElasticsearchPost(saved));

        webSocketService.sendInsertPostEvent(postDTO);
        seoCacheListenerProxy.rewriteCachedPage(saved.getId());
        seoCacheListenerProxy.rewriteCachedIndex();

        return postConverter.convertToDto(saved, userAccount);
    }

    public PostDTOWithAuthorization updatePost(UserAccountDetailsDTO userAccount, @NotNull PostDTO postDTO) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        Post found = postRepository.findById(postDTO.getId()).orElseThrow(()->new IllegalArgumentException("Post with id " + postDTO.getId() + " not found"));
        Post updatedEntity = postConverter.convertToPost(postDTO, found);
        updatedEntity.setEditDateTime(LocalDateTime.now(ZoneOffset.UTC));
        Post saved = postRepository.saveAndFlush(updatedEntity);
        indexPostRepository.save(postConverter.toElasticsearchPost(saved));

        webSocketService.sendUpdatePostEvent(postDTO);
        seoCacheListenerProxy.rewriteCachedPage(saved.getId());
        seoCacheListenerProxy.rewriteCachedIndex();

        return postConverter.convertToDto(saved, userAccount);
    }

    public PostDTO convertToPostDTOWithCleanTags(Post post) {
        PostDTO postDTO = postConverter.convertToPostDTO(post);
        IndexPost byId = indexPostRepository
                .findById(post.getId())
                .orElseThrow(()->new DataNotFoundException("post "+post.getId()+" not found in fulltext store"));
        postDTO.setText(byId.getText());
        return postDTO;
    }

    public PostDTO convertToPostDTOWithCleanTags(PostDTO post) {
        PostConverter.cleanTags(post);
        return post;
    }

    private AbstractQueryBuilder noDraftFilterElastic(UserAccountDetailsDTO userAccountDetailsDTO){
        if (userAccountDetailsDTO==null) {
            return termQuery(FIELD_DRAFT, false);
        } else {
            if (roleHierarchy.getReachableGrantedAuthorities(userAccountDetailsDTO.getAuthorities()).contains(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name()))){
                return matchAllQuery();
            } else {
                return boolQuery()
                        .should(termQuery(FIELD_DRAFT, false))
                        .should(termQuery(FIELD_OWNER, userAccountDetailsDTO.getId()));
            }
        }
    }

    private Map<String, Object> noDraftFilterJdbc(UserAccountDetailsDTO userAccountDetailsDTO){
        Map<String, Object> countParams = new HashMap<>();
        if (userAccountDetailsDTO==null) {
            countParams.put("currentUserId", null);
            countParams.put("isAdmin", false);
        } else {
            countParams.put("currentUserId", userAccountDetailsDTO.getId());
            countParams.put("isAdmin", roleHierarchy.getReachableGrantedAuthorities(userAccountDetailsDTO.getAuthorities()).contains(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name())));
        }
        return countParams;
    }

    public Wrapper<PostDTO> getPosts(int page, int size, String searchString, @Nullable UserAccountDetailsDTO currentUser){
        page = PageUtils.fixPage(page);
        size = PageUtils.fixSize(size);
        searchString = StringUtils.trimWhitespace(searchString);

        List<PostDTO> postsResult;

        if (StringUtils.isEmpty(searchString)) {
            PageRequest pageRequest = PageRequest.of(page, size);

            SearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withSort(new FieldSortBuilder(FIELD_ID).order(SortOrder.DESC))
                    .withIndices(INDEX)
                    .withQuery(boolQuery()
                            .must(noDraftFilterElastic(currentUser))
                    )
                    .withPageable(pageRequest)
                    .build();
            // https://stackoverflow.com/questions/37049764/how-to-provide-highlighting-with-spring-data-elasticsearch/37163711#37163711
            postsResult = getPostDTOS(searchQuery);


        } else {
            PageRequest pageRequest = PageRequest.of(page, size);

            SearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withSort(new FieldSortBuilder(FIELD_ID).order(SortOrder.DESC))
                    .withIndices(INDEX)
                    .withQuery(boolQuery()
                            .must(
                                    boolQuery()
                                            .should(matchPhrasePrefixQuery(FIELD_TEXT, searchString).slop(searchFieldTextSlop))
                                            .should(matchPhrasePrefixQuery(FIELD_TITLE, searchString).slop(searchFieldTextSlop))
                            )

                            .must(noDraftFilterElastic(currentUser))
                    )
                    .withHighlightFields(
                            new HighlightBuilder.Field(FIELD_TEXT).preTags("<b>").postTags("</b>").numOfFragments(highlightFieldTextNumOfFragments).fragmentSize(highlightFieldTextFragmentSize),
                            new HighlightBuilder.Field(FIELD_TITLE).preTags("<u>").postTags("</u>").numOfFragments(highlightFieldTitleNumOfFragments).fragmentSize(highlightFieldTitleFragmentSize)
                    )
                    .withPageable(pageRequest)
                    .build();
            // https://stackoverflow.com/questions/37049764/how-to-provide-highlighting-with-spring-data-elasticsearch/37163711#37163711
            postsResult = getPostDTOS(searchQuery);
        }

        NativeSearchQuery countQuery = new NativeSearchQueryBuilder()
                .withIndices(INDEX)
                .withQuery(boolQuery().must(noDraftFilterElastic(currentUser)))
                .build();
        long totalCount = elasticsearchTemplate.count(countQuery);

        return new Wrapper<>(postsResult, totalCount);
    }

    private List<PostDTO> getPostDTOS(SearchQuery searchQuery) {
        List<PostDTO> postsResult;
        Page<IndexPost> fulltextResult = elasticsearchTemplate.queryForPage(searchQuery, IndexPost.class, searchResultMapper);

        postsResult = new ArrayList<>();
        for (IndexPost fulltextPost: fulltextResult){

            var params = new HashMap<String, Object>();
            params.put("postId", fulltextPost.getId());
            LOGGER.debug("Will search in postgres by id="+fulltextPost.getId());
            PostDTO postDTO = jdbcTemplate.queryForObject(
                    rowMapperWithoutTextTitle.getBaseSql() + " where p.id = :postId",
                    params,
                    rowMapperWithoutTextTitle
            );
            if (postDTO == null){
                throw new DataNotFoundException("post not found in db");
            }
            postDTO.setText(fulltextPost.getText());
            postDTO.setTitle(fulltextPost.getTitle());

            postsResult.add(postDTO);
        }
        return postsResult;
    }

    public Wrapper<PostDTO> findByOwnerId(Pageable springDataPage, Long userId, UserAccountDetailsDTO userAccountDetailsDTO) {
        int limit = springDataPage.getPageSize();
        long offset = springDataPage.getOffset();

        var params = new HashMap<String, Object>();
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("userId", userId);

        var postsResult = jdbcTemplate.query(
                rowMapper.getBaseSql() +
                        " where u.id = :userId " +
                        "  order by p.id desc " +
                        "limit :limit offset :offset\n",
                params,
                rowMapper
        );

        List<PostDTO> list = postsResult.stream()
                .map(this::convertToPostDTOWithCleanTags)
                .collect(Collectors.toList());
        var countParams = new HashMap<String, Object>();
        countParams.put("userId", userId);
        countParams.putAll(noDraftFilterJdbc(userAccountDetailsDTO));
        long count = jdbcTemplate.queryForObject("select count(*) from posts.post p where p.owner_id = :userId and (p.draft = FALSE OR ((:currentUserId\\:\\:bigint) = (:userId\\:\\:bigint)) OR :isAdmin = TRUE)", countParams, long.class);
        return new Wrapper<>(list, count);
    }

    public void deletePost(UserAccountDetailsDTO userAccount, long postId) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        commentRepository.deleteByPostId(postId);
        postRepository.deleteById(postId);
        postRepository.flush();
        indexPostRepository.deleteById(postId);

        webSocketService.sendDeletePostEvent(postId);
        seoCacheService.removeAllPagesCache(postId);
        seoCacheListenerProxy.rewriteCachedIndex();
    }



    private String getKey() {
        return "elasticsearch:"+ IndexPost.INDEX+":building";
    }

    private void refreshFulltextIndex() {
        LOGGER.info("Starting refreshing elasticsearch index {}", IndexPost.INDEX);
        final Collection<Long> postIds = postRepository.findPostIds();

        for (Long id: postIds) {
            Optional<com.github.nkonev.blog.entity.jpa.Post> post = postRepository.findById(id);
            if (post.isPresent()) {
                com.github.nkonev.blog.entity.jpa.Post jpaPost = post.get();
                LOGGER.info("Copying PostgreSQL -> Elasticsearch post id={}", id);
                indexPostRepository.save(postConverter.toElasticsearchPost(jpaPost));
            }
        }

        removeOrphansFromIndex();
        LOGGER.info("Finished refreshing elasticsearch index {}", IndexPost.INDEX);
    }

    public void removeOrphansFromIndex() {
        List<Long> toDeleteFromIndex = new ArrayList<>();
        for(int page=0; ;page++) {
            PageRequest pageRequest = PageRequest.of(page, chunkSize);
            final String[] includes = {"_"};
            SearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withIndices(INDEX)
                    .withQuery(matchAllQuery())
                    .withPageable(pageRequest)
                    .withSourceFilter(new FetchSourceFilter(includes, null))
                    .build();
            List<String> idsString = elasticsearchTemplate.queryForIds(searchQuery);
            LOGGER.info("Get {} index ids", idsString.size());
            if (idsString.isEmpty()) {
                break;
            }
            idsString.stream().map(Long::valueOf).forEach(id -> {
                if (!postRepository.existsById(id)){
                    toDeleteFromIndex.add(id);
                }
            });
        }
        LOGGER.info("Found {} orphan posts in index", toDeleteFromIndex.size());
        for (Long id: toDeleteFromIndex) {
            LOGGER.info("Deleting orphan post id={} from index", id);
            indexPostRepository.deleteById(id);
        }
    }

    public void refreshFulltextIndex(boolean ignoreInProgress){
        final String key = getKey();
        final boolean wasSet = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "true"));

        if (wasSet || ignoreInProgress) {
            LOGGER.info("Probe is successful, so we'll refresh elasticsearch index");
            redisTemplate.expire(key, timeout, timeUnit);

            refreshFulltextIndex();

            redisTemplate.delete(key);
            LOGGER.info("Successful delete probe");
        } else {
            LOGGER.info("Probe isn't successful, so we won't refresh elasticsearch index");
        }

    }
}
