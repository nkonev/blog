package com.github.nkonev.blog.controllers;

import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.converter.PostConverter;
import com.github.nkonev.blog.dto.*;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.exception.BadRequestException;
import com.github.nkonev.blog.exception.DataNotFoundException;
import com.github.nkonev.blog.repo.jpa.CommentRepository;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import com.github.nkonev.blog.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Value("${custom.postgres.fulltext.reg-config}")
    private String regConfig;

    private final RowMapper<PostDTO> rowMapper = (resultSet, i) -> new PostDTO(
            resultSet.getLong("id"),
            resultSet.getString("title"),
            resultSet.getString("text_column"),
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

        Map<String, Object> params = new HashMap<>();
        params.put("search_string", searchString);
        params.put("offset", PageUtils.getOffset(page, size));
        params.put("limit", size);

        List<PostDTO> posts;

        if (StringUtils.isEmpty(searchString)) {
            posts = jdbcTemplate.query(
                    "select " +
                            "p.id, " +
                            "p.title, " +
                            "substring(p.text_no_tags from 0 for 700) as text_column, " +
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
        } else {
            posts = jdbcTemplate.query(
                    "with tsq as (select to_tsquery("+regConfig+", array_to_string(array(select replace(i, ':', '\\:')||':*' from unnest(tsvector_to_array(to_tsvector("+regConfig+", :search_string))) as i), ' & ')) ) \n" +
                            "select\n" +
                            " fulltext_result.id, \n" +
                            " ts_headline("+regConfig+", fulltext_result.title, (select * from tsq), 'StartSel=\"<u>\", StopSel=\"</u>\"') as title, \n" +
                            " ts_headline("+regConfig+", fulltext_result.text_no_tags, (select * from tsq), 'StartSel=\"<b>\", StopSel=\"</b>\", MaxWords=165, MinWords=85, MaxFragments=5') as text_column, \n" +
                            " fulltext_result.title_img,\n" +
                            " fulltext_result.create_date_time,\n" +
                            " u.id as owner_id," +
                            " u.username as owner_login," +
                            " u.facebook_id as owner_facebook_id," +
                            " u.avatar as owner_avatar \n" +
                            "from (\n" +
                            "  select id, title, text_no_tags, title_img, create_date_time, owner_id \n" +
                            "  from posts.post \n" +
                            "  where to_tsvector("+regConfig+", title || ' ' || text_no_tags) @@ (select * from tsq) " +
                            " union " +
                            "  select id, title, text_no_tags, title_img, create_date_time, owner_id \n" +
                            "  from posts.post \n" +
                            "  where lower(title || ' ' || text_no_tags) LIKE '%' || lower(:search_string) || '%' " +
                            " order by id desc " +
                            " limit :limit offset :offset\n" +
                            ") as fulltext_result " +
                            "join auth.users u on fulltext_result.owner_id = u.id " +
                            "order by id desc" +
                            ";",
                    params,
                    rowMapper
            );
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
