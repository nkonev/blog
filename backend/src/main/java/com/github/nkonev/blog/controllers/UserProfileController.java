package com.github.nkonev.blog.controllers;

import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.converter.UserAccountConverter;
import com.github.nkonev.blog.dto.*;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.exception.UserAlreadyPresentException;
import com.github.nkonev.blog.repo.jpa.CommentRepository;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import com.github.nkonev.blog.security.BlogSecurityService;
import com.github.nkonev.blog.security.BlogUserDetailsService;
import com.github.nkonev.blog.services.PostService;
import com.github.nkonev.blog.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.Session;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by nik on 08.06.17.
 */
@RequestMapping(Constants.Urls.API)
@RestController
@Transactional
public class UserProfileController {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BlogUserDetailsService blogUserDetailsService;

    @Autowired
    private BlogSecurityService blogSecurityService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserAccountConverter userAccountConverter;

    /**
     *
     * @param userAccount
     * @return current logged in profile
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = Constants.Urls.PROFILE)
    public UserAccountDTO checkAuthenticated(@AuthenticationPrincipal UserAccountDetailsDTO userAccount) throws MalformedURLException {
        return UserAccountConverter.convertToUserAccountDTO(userAccount);
    }

    @GetMapping(value = Constants.Urls.USER)
    public UserListWrapper getUsersGet(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount,
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required=false, defaultValue = "") String searchString
    ) {
        PageRequest springDataPage = PageRequest.of(PageUtils.fixPage(page), PageUtils.fixSize(size), Sort.Direction.ASC, "id");
        searchString = searchString.trim();

        Page<UserAccount> resultPage = userAccountRepository.findByUsernameContainsIgnoreCase(springDataPage, searchString);

        return new UserListWrapper(
                resultPage.getContent().stream().map(getConvertToUserAccountDTO(userAccount)).collect(Collectors.toList()),
                resultPage.getTotalElements(),
                blogSecurityService.hasSessionManagementPermission(userAccount)
        );
    }

    @GetMapping(value = Constants.Urls.USER + Constants.Urls.USER_ID + Constants.Urls.POSTS)
    public Wrapper<PostDTO> getUserPosts(
            @PathVariable(Constants.PathVariables.USER_ID) Long userId,
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size
    ) {
        PageRequest springDataPage = PageRequest.of(PageUtils.fixPage(page), PageUtils.fixSize(size), Sort.Direction.DESC, "id");

        return postService.findByOwnerId(springDataPage, userId);
    }


    private Function<UserAccount, UserAccountDTO> getConvertToUserAccountDTO(UserAccountDetailsDTO currentUser) {
        return userAccount -> userAccountConverter.convertToUserAccountDTOExtended(currentUser, userAccount);
    }

    @GetMapping(value = Constants.Urls.USER+ Constants.Urls.USER_ID)
    public UserAccountDTO getUser(
            @PathVariable(Constants.PathVariables.USER_ID) Long userId,
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount
        ) {
        UserAccount userAccountEntity = userAccountRepository.findById(userId).orElseThrow(() -> new RuntimeException("user with id="+ userId + " not found"));
        if (userAccount!=null && userAccount.getId().equals(userAccountEntity.getId())){
            return UserAccountConverter.getUserSelfProfile(userAccount, userAccountEntity);
        } else {
            return userAccountConverter.convertToUserAccountDTO(userAccountEntity);
        }
    }

    @PostMapping(Constants.Urls.PROFILE)
    @PreAuthorize("isAuthenticated()")
    public EditUserDTO editProfile(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount,
            @RequestBody @Valid EditUserDTO userAccountDTO
    ) {
        if (userAccount == null) {
            throw new RuntimeException("Not authenticated user can't edit any user account. It can occurs due inpatient refactoring.");
        }

        UserAccount exists = userAccountRepository.findById(userAccount.getId()).orElseThrow(()-> new RuntimeException("Authenticated user account not found in database"));

        // check email already present
        if (exists.getEmail()!=null && !exists.getEmail().equals(userAccountDTO.getEmail()) && userAccountRepository.findByEmail(userAccountDTO.getEmail()).isPresent()) {
            // throw new UserAlreadyPresentException("User with email '" + userAccountDTO.getEmail() + "' is already present");
            return UserAccountConverter.convertToEditUserDto(exists); // we care for email leak...
        }
        // check login already present
        if (!exists.getUsername().equals(userAccountDTO.getLogin()) && userAccountRepository.findByUsername(userAccountDTO.getLogin()).isPresent()) {
            throw new UserAlreadyPresentException("User with login '" + userAccountDTO.getLogin() + "' is already present");
        }

        UserAccountConverter.updateUserAccountEntity(userAccountDTO, exists, passwordEncoder);
        exists = userAccountRepository.save(exists);

        blogUserDetailsService.refreshUserDetails(exists);

        return UserAccountConverter.convertToEditUserDto(exists);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(Constants.Urls.SESSIONS+"/my")
    public Map<String, Session> mySessions(@AuthenticationPrincipal UserAccountDetailsDTO userDetails){
        return blogUserDetailsService.getMySessions(userDetails);
    }

    @PreAuthorize("@blogSecurityService.hasSessionManagementPermission(#userAccount)")
    @GetMapping(Constants.Urls.SESSIONS)
    public Map<String, Session> sessions(@AuthenticationPrincipal UserAccountDetailsDTO userAccount, @RequestParam("userId") long userId){
        return blogUserDetailsService.getSessions(userId);
    }

    @PreAuthorize("@blogSecurityService.hasSessionManagementPermission(#userAccount)")
    @DeleteMapping(Constants.Urls.SESSIONS)
    public void killSessions(@AuthenticationPrincipal UserAccountDetailsDTO userAccount, @RequestParam("userId") long userId){
        blogUserDetailsService.killSessions(userId);
    }

    @PreAuthorize("@blogSecurityService.canLock(#userAccountDetailsDTO, #lockDTO)")
    @PostMapping(Constants.Urls.USER + Constants.Urls.LOCK)
    public UserAccountDTOExtended setLocked(@AuthenticationPrincipal UserAccountDetailsDTO userAccountDetailsDTO, @RequestBody LockDTO lockDTO){
        UserAccount userAccount = blogUserDetailsService.getUserAccount(lockDTO.getUserId());
        if (lockDTO.isLock()){
            blogUserDetailsService.killSessions(lockDTO.getUserId());
        }
        userAccount.setLocked(lockDTO.isLock());

        return userAccountConverter.convertToUserAccountDTOExtended(userAccountDetailsDTO, userAccount);
    }

    @PreAuthorize("@blogSecurityService.canDelete(#userAccountDetailsDTO, #userId)")
    @DeleteMapping(Constants.Urls.USER)
    public void deleteUser(@AuthenticationPrincipal UserAccountDetailsDTO userAccountDetailsDTO, @RequestParam("userId") long userId){
        deleteCommon(userId);
    }

    @PreAuthorize("@blogSecurityService.canSelfDelete(#userAccountDetailsDTO)")
    @DeleteMapping(Constants.Urls.PROFILE)
    public void selfDeleteUser(@AuthenticationPrincipal UserAccountDetailsDTO userAccountDetailsDTO){
        long userId = userAccountDetailsDTO.getId();
        deleteCommon(userId);
    }

    private void deleteCommon(long userId) {
        blogUserDetailsService.killSessions(userId);
        UserAccount deleted = userAccountRepository.findByUsername(Constants.DELETED).orElseThrow();
        postRepository.moveToAnotherUser(userId, deleted.getId());
        commentRepository.moveToAnotherUser(userId, deleted.getId());
        userAccountRepository.deleteById(userId);
    }
}