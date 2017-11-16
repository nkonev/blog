package com.github.nkonev.controllers;

import com.github.nkonev.Constants;
import com.github.nkonev.dto.*;
import com.github.nkonev.exception.DataNotFoundException;
import com.github.nkonev.repo.jpa.CommentRepository;
import com.github.nkonev.utils.PageUtils;
import com.github.nkonev.converter.PostConverter;
import com.github.nkonev.entity.jpa.Post;
import com.github.nkonev.entity.jpa.UserAccount;
import com.github.nkonev.exception.BadRequestException;
import com.github.nkonev.repo.jpa.PostRepository;
import com.github.nkonev.repo.jpa.UserAccountRepository;
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
import java.util.Collections;
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
                    resultSet.getString("owner_avatar")
            )
    );

    @GetMapping(Constants.Uls.API + Constants.Uls.POST)
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
                            "substring(p.text_no_tags from 0 for 700) || '...' as text_column, " +
                            "p.title_img, " +
                            "p.create_date_time," +
                            "u.id as owner_id," +
                            "u.username as owner_login," +
                            "u.avatar as owner_avatar \n" +
                            "  from posts.post p join auth.users u on p.owner_id = u.id \n" +
                            "  order by id desc " +
                            "limit :limit offset :offset\n",
                    params,
                    rowMapper
            );
        } else {
            posts = jdbcTemplate.query(
                    "with tsq_ft as (select plainto_tsquery(" + regConfig + ", :search_string)), \n" +
                            " tsq_co as (select to_tsquery("+regConfig+", array_to_string(array(select i||':*' from unnest(tsvector_to_array(to_tsvector("+regConfig+", :search_string))) as i), ' & ')) ) \n" +

                            "select " +
                            "fulltext_union_result.id as id, " +
                            "fulltext_union_result.title as title, " +
                            "fulltext_union_result.text_column as text_column, " +
                            "fulltext_union_result.title_img as title_img, " +
                            "fulltext_union_result.create_date_time as create_date_time, " +
                            " u.id as owner_id," +
                            " u.username as owner_login," +
                            " u.avatar as owner_avatar \n" +
                            " from (" +
                                "select\n" +
                                " fulltext_result.id, \n" +
                                " ts_headline(" + regConfig + ", fulltext_result.title, (select * from tsq_ft), 'StartSel=\"<u>\", StopSel=\"</u>\"') as title, \n" +
                                " ts_headline(" + regConfig + ", fulltext_result.text_no_tags, (select * from tsq_ft), 'StartSel=\"<b>\", StopSel=\"</b>\", MaxWords=165, MinWords=85, MaxFragments=5') as text_column, \n" +
                                " fulltext_result.title_img,\n" +
                                " fulltext_result.create_date_time, \n" +
                                " fulltext_result.owner_id " +
                                "from (\n" +
                                "  select id, title, text_no_tags, title_img, create_date_time, owner_id \n" +
                                "  from posts.post \n" +
                                "  where to_tsvector(" + regConfig + ", title || ' ' || text_no_tags) @@ (select * from tsq_ft)" +
                                "  order by id desc " +
                                "  limit :limit offset :offset\n" +
                                ") as fulltext_result " +

                                "union " +

                                "select\n" +
                                " contains_result.id, \n" +
                                " ts_headline(" + regConfig + ", contains_result.title, (select * from tsq_co), 'StartSel=\"<u>\", StopSel=\"</u>\"') as title, \n" +
                                " ts_headline(" + regConfig + ", contains_result.text_no_tags, (select * from tsq_co), 'StartSel=\"<b>\", StopSel=\"</b>\", MaxWords=165, MinWords=85, MaxFragments=5') as text_column, \n" +
                                " contains_result.title_img,\n" +
                                " contains_result.create_date_time, \n" +
                                " contains_result.owner_id " +
                                "from (\n" +
                                "  select id, title, text_no_tags, title_img, create_date_time, owner_id \n" +
                                "  from posts.post \n" +
                                "  where to_tsvector(" + regConfig + ", title || ' ' || text_no_tags) @@ (select * from tsq_co)" +
                                "  order by id desc " +
                                "  limit :limit offset :offset\n" +
                                ") as contains_result" +
                            ") as fulltext_union_result " +
                            "join auth.users u on fulltext_union_result.owner_id = u.id " +
                            "order by id desc " +

                            ";",
                    params,
                    rowMapper
            );
        }

        return posts;
    }

    @GetMapping(Constants.Uls.API + Constants.Uls.POST + Constants.Uls.POST_ID)
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

    @PreAuthorize("@blogSecurityService.hasPostPermission(#userAccount, T(com.github.nkonev.security.permissions.PostPermissions).READ_MY)")
    @GetMapping(Constants.Uls.API + Constants.Uls.POST + Constants.Uls.MY)
    public List<PostDTO> getMyPosts(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required = false, defaultValue = "") String searchString // TODO implement
    ) {

        PageRequest springDataPage = new PageRequest(PageUtils.fixPage(page), PageUtils.fixSize(size));

        return postRepository
                .findMyPosts(springDataPage).getContent()
                .stream()
                .map(postConverter::convertToPostDTOWithCleanTags)
                .collect(Collectors.toList());
    }

    // https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#el-common-built-in
    @PreAuthorize("@blogSecurityService.hasPostPermission(#userAccount, T(com.github.nkonev.security.permissions.PostPermissions).CREATE)")
    @PostMapping(Constants.Uls.API + Constants.Uls.POST)
    public PostDTOWithAuthorization addPost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @RequestBody @NotNull PostDTO postDTO
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        if (postDTO.getId() != 0) {
            throw new BadRequestException("id cannot be set");
        }
        Post fromWeb = postConverter.convertToPost(postDTO, null);
        UserAccount ua = userAccountRepository.findOne(userAccount.getId()); // Hibernate caches it
        Assert.notNull(ua, "User account not found");
        fromWeb.setOwner(ua);
        Post saved = postRepository.save(fromWeb);
        return postConverter.convertToDto(saved, userAccount);
    }

    @PreAuthorize("@blogSecurityService.hasPostPermission(#postDTO, #userAccount, T(com.github.nkonev.security.permissions.PostPermissions).EDIT)")
    @PutMapping(Constants.Uls.API + Constants.Uls.POST)
    public PostDTOWithAuthorization updatePost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @RequestBody @NotNull PostDTO postDTO
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        Post found = postRepository.findOne(postDTO.getId());
        Assert.notNull(found, "Post with id " + postDTO.getId() + " not found");
        Post updatedEntity = postConverter.convertToPost(postDTO, found);
        Post saved = postRepository.save(updatedEntity);
        return postConverter.convertToDto(saved, userAccount);
    }

    @PreAuthorize("@blogSecurityService.hasPostPermission(#postId, #userAccount, T(com.github.nkonev.security.permissions.PostPermissions).DELETE)")
    @DeleteMapping(Constants.Uls.API + Constants.Uls.POST + Constants.Uls.POST_ID)
    public void deletePost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @PathVariable(Constants.PathVariables.POST_ID) long postId
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        commentRepository.deleteByPostId(postId);
        postRepository.delete(postId);
        postRepository.flush();
    }
}
