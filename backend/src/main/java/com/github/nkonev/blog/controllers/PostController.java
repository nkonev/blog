package com.github.nkonev.blog.controllers;

import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.converter.PostConverter;
import com.github.nkonev.blog.dto.*;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.exception.BadRequestException;
import com.github.nkonev.blog.exception.DataNotFoundException;
import com.github.nkonev.blog.repo.elasticsearch.IndexPostRepository;
import com.github.nkonev.blog.repo.jpa.CommentRepository;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import com.github.nkonev.blog.utils.PageUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.nkonev.blog.entity.elasticsearch.Post.FIELD_TEXT;
import static com.github.nkonev.blog.entity.elasticsearch.Post.FIELD_TITLE;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Transactional
@RestController
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PostConverter postConverter;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private IndexPostRepository indexPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Value("${custom.postgres.fulltext.reg-config}")
    private String regConfig;

    private final RowMapper<PostDTO> rowMapper = (resultSet, i) -> new PostDTO(
            resultSet.getLong("id"),
            resultSet.getString("title"),
            "post clean text",
            resultSet.getString("title_img"),
            resultSet.getObject("create_date_time", LocalDateTime.class),
            new UserAccountDTO(
                    resultSet.getLong("owner_id"),
                    resultSet.getString("owner_login"),
                    resultSet.getString("owner_avatar"),
                    resultSet.getString("owner_facebook_id")
            )
    );

    @GetMapping(Constants.Urls.API + Constants.Urls.POST)
    public List<PostDTO> getPosts(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required = false, defaultValue = "") String searchString
    ) {
        page = PageUtils.fixPage(page);
        size = PageUtils.fixSize(size);
        searchString = StringUtils.trimWhitespace(searchString);


        List<PostDTO> posts;

        if (StringUtils.isEmpty(searchString)) {
            var params = new HashMap<String, Object>();
            params.put("search_string", searchString);
            params.put("offset", PageUtils.getOffset(page, size));
            params.put("limit", size);

            posts = jdbcTemplate.query(
                    "select " +
                            "p.id, " +
                            "p.title, " +
                            "p.title_img, " +
                            "p.create_date_time," +
                            "u.id as owner_id," +
                            "u.username as owner_login," +
                            "u.facebook_id as owner_facebook_id," +
                            "u.avatar as owner_avatar \n" +
                            "  from posts.post p join auth.users u on p.owner_id = u.id \n" +
                            "  order by id desc " +
                            "limit :limit offset :offset\n",
                    params,
                    rowMapper
            );

            posts.forEach(postDTO -> {
                com.github.nkonev.blog.entity.elasticsearch.Post post = indexPostRepository
                        .findById(postDTO.getId())
                        .orElseThrow(() -> new DataNotFoundException("post not found in elasticsearch store"));
                postDTO.setText(post.getText());
            });

        } else {
            // todo to separate post service
            PageRequest pageRequest = PageRequest.of(page, size);

            SearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery()
                            .should(matchPhrasePrefixQuery(FIELD_TEXT, searchString))
                            .should(matchPhrasePrefixQuery(FIELD_TITLE, searchString))
                    )
                    .withHighlightFields(
                            new HighlightBuilder.Field(FIELD_TEXT).preTags("<b>").postTags("</b>").numOfFragments(5).fragmentSize(150),
                            new HighlightBuilder.Field(FIELD_TITLE).preTags("<u>").postTags("</u>").numOfFragments(1).fragmentSize(150)
                    )
                    .withPageable(pageRequest)
                    .build();
            // https://stackoverflow.com/questions/37049764/how-to-provide-highlighting-with-spring-data-elasticsearch/37163711#37163711
            Page<com.github.nkonev.blog.entity.elasticsearch.Post> fulltextResult = elasticsearchTemplate.queryForPage(searchQuery, com.github.nkonev.blog.entity.elasticsearch.Post.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                    List<com.github.nkonev.blog.entity.elasticsearch.Post> list = new ArrayList<>();
                    for (SearchHit searchHit : response.getHits()) {
                        if (response.getHits().getHits().length <= 0) {
                            return new AggregatedPageImpl<T>((List<T>) list);
                        }
                        com.github.nkonev.blog.entity.elasticsearch.Post tempPost = new com.github.nkonev.blog.entity.elasticsearch.Post();
                        tempPost.setId(Long.valueOf(searchHit.getId()));

                        String title = (String) searchHit.getSource().get(FIELD_TITLE);
                        String text = (String) searchHit.getSource().get(FIELD_TEXT);

                        HighlightField highlightedTitle = searchHit.getHighlightFields().get(FIELD_TITLE);
                        HighlightField highlightedText = searchHit.getHighlightFields().get(FIELD_TEXT);

                        if (highlightedTitle!=null && highlightedTitle.getFragments()!=null && highlightedTitle.getFragments().length>0){
                            title = highlightedTitle.getFragments()[0].toString();
                        }

                        if (highlightedText!=null && highlightedText.getFragments()!=null && highlightedText.getFragments().length>0){
                            text = Arrays.stream(highlightedText.getFragments()).map(Text::toString).collect(Collectors.joining("... "));
                        }

                        tempPost.setTitle(title);
                        tempPost.setText(text);
                        list.add(tempPost);
                    }
                    return new AggregatedPageImpl<T>((List<T>) list);
                }
            });

            posts = new ArrayList<>();
            for (com.github.nkonev.blog.entity.elasticsearch.Post fulltextPost: fulltextResult){

                var params = new HashMap<String, Object>();
                params.put("id", fulltextPost.getId());

                PostDTO postDTO = jdbcTemplate.queryForObject(
                        "select " +
                                "p.id, " +
                                "p.title, " +
                                "p.title_img, " +
                                "p.create_date_time," +
                                "u.id as owner_id," +
                                "u.username as owner_login," +
                                "u.facebook_id as owner_facebook_id," +
                                "u.avatar as owner_avatar \n" +
                                "  from posts.post p join auth.users u on p.owner_id = u.id \n" +
                                " where p.id = :id",
                        params,
                        rowMapper
                );
                if (postDTO == null){
                    throw new DataNotFoundException("post not found in postgres");
                }
                postDTO.setText(fulltextPost.getText());
                postDTO.setTitle(fulltextPost.getTitle());

                posts.add(postDTO);
            }
        }

        return posts;
    }

    public static class RandomPostDTO {
        private long id;
        private String title;

        public RandomPostDTO(long id, String title) {
            this.id = id;
            this.title = title;
        }

        public long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }

    @GetMapping(Constants.Urls.API + Constants.Urls.POST + Constants.Urls.RANDOM)
    public List<RandomPostDTO> getRandomPosts() {

        return jdbcTemplate.query(
                "select id, title from posts.post order by random() limit 10",
                (resultSet, i) -> new RandomPostDTO(resultSet.getLong("id"), resultSet.getString("title"))
        );
    }


    @GetMapping(Constants.Urls.API + Constants.Urls.POST + Constants.Urls.POST_ID)
    public PostDTOExtended getPost(
            @PathVariable(Constants.PathVariables.POST_ID) long id,
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount // null if not authenticated
    ) {
        return postRepository
                .findById(id)
                .map(post -> postConverter.convertToDtoExtended(post, userAccount))
                .orElseThrow(() -> new DataNotFoundException("Post " + id + " not found"));
    }


    // ================================================= secured

    @PreAuthorize("@blogSecurityService.hasPostPermission(#userAccount, T(com.github.nkonev.blog.security.permissions.PostPermissions).READ_MY)")
    @GetMapping(Constants.Urls.API + Constants.Urls.POST + Constants.Urls.MY)
    public List<PostDTO> getMyPosts(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required = false, defaultValue = "") String searchString // TODO implement
    ) {

        PageRequest springDataPage = PageRequest.of(PageUtils.fixPage(page), PageUtils.fixSize(size));

        return postRepository
                .findMyPosts(springDataPage).getContent()
                .stream()
                .map(postConverter::convertToPostDTOWithCleanTags)
                .collect(Collectors.toList());
    }

    // https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#el-common-built-in
    @PreAuthorize("@blogSecurityService.hasPostPermission(#userAccount, T(com.github.nkonev.blog.security.permissions.PostPermissions).CREATE)")
    @PostMapping(Constants.Urls.API + Constants.Urls.POST)
    public PostDTOWithAuthorization addPost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @RequestBody @NotNull PostDTO postDTO
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        if (postDTO.getId() != 0) {
            throw new BadRequestException("id cannot be set");
        }
        Post fromWeb = postConverter.convertToPost(postDTO, null);
        UserAccount ua = userAccountRepository.findById(userAccount.getId()).orElseThrow(()->new IllegalArgumentException("User account not found")); // Hibernate caches it
        fromWeb.setOwner(ua);
        Post saved = postRepository.save(fromWeb);
        return postConverter.convertToDto(saved, userAccount);
    }

    @PreAuthorize("@blogSecurityService.hasPostPermission(#postDTO, #userAccount, T(com.github.nkonev.blog.security.permissions.PostPermissions).EDIT)")
    @PutMapping(Constants.Urls.API + Constants.Urls.POST)
    public PostDTOWithAuthorization updatePost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @RequestBody @NotNull PostDTO postDTO
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        Post found = postRepository.findById(postDTO.getId()).orElseThrow(()->new IllegalArgumentException("Post with id " + postDTO.getId() + " not found"));
        Post updatedEntity = postConverter.convertToPost(postDTO, found);
        Post saved = postRepository.saveAndFlush(updatedEntity);
        return postConverter.convertToDto(saved, userAccount);
    }

    @PreAuthorize("@blogSecurityService.hasPostPermission(#postId, #userAccount, T(com.github.nkonev.blog.security.permissions.PostPermissions).DELETE)")
    @DeleteMapping(Constants.Urls.API + Constants.Urls.POST + Constants.Urls.POST_ID)
    public void deletePost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @PathVariable(Constants.PathVariables.POST_ID) long postId
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        commentRepository.deleteByPostId(postId);
        postRepository.deleteById(postId);
        postRepository.flush();
    }
}
