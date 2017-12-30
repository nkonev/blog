package com.github.nkonev.controllers;

import com.github.nkonev.Constants;
import com.github.nkonev.converter.UserAccountConverter;
import com.github.nkonev.dto.*;
import com.github.nkonev.entity.jpa.UserAccount;
import com.github.nkonev.exception.UserAlreadyPresentException;
import com.github.nkonev.repo.jpa.UserAccountRepository;
import com.github.nkonev.security.BlogSecurityService;
import com.github.nkonev.security.BlogUserDetailsService;
import com.github.nkonev.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nik on 08.06.17.
 */
@RequestMapping(Constants.Uls.API)
@RestController
@PreAuthorize("isAuthenticated()")
@Transactional
public class UserProfileController {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BlogUserDetailsService blogUserDetailsService;

    @Autowired
    private BlogSecurityService blogSecurityService;

    /**
     *
     * @param userAccount
     * @return current logged in profile
     */
    @GetMapping(value = Constants.Uls.PROFILE)
    public UserAccountDTO checkAuthenticated(@AuthenticationPrincipal UserAccountDetailsDTO userAccount) throws MalformedURLException {
        return UserAccountConverter.convertToUserAccountDTO(userAccount);
    }

    @GetMapping(value = Constants.Uls.USER)
    public UserListWrapper getUsersGet(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount,
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required=false, defaultValue = "") String searchString
    ) {
        PageRequest springDataPage = new PageRequest(PageUtils.fixPage(page), PageUtils.fixSize(size), Sort.Direction.ASC, "id");

        Page<UserAccount> resultPage = userAccountRepository.findByUsernameContains(springDataPage, searchString);
        return new UserListWrapper(
                resultPage.getContent().stream().map(UserAccountConverter::convertToUserAccountDTO).collect(Collectors.toList()),
                resultPage.getTotalElements(),
                blogSecurityService.hasSessionManagementPermission(userAccount)
        );
    }

    @GetMapping(value = Constants.Uls.USER+Constants.Uls.USER_ID)
    public UserAccountDTO getUser(
            @PathVariable(Constants.PathVariables.USER_ID) Long userId,
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount
        ) {
        UserAccount userAccountEntity = userAccountRepository.findById(userId).orElseThrow(() -> new RuntimeException("user with id="+ userId + " not found"));
        if (userAccount!=null && userAccount.getId().equals(userAccountEntity.getId())){
            return UserAccountConverter.getUserSelfProfile(userAccount);
        } else {
            return UserAccountConverter.convertToUserAccountDTO(userAccountEntity);
        }
    }

    @PostMapping(Constants.Uls.PROFILE)
    public EditUserDTO editProfile(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount,
            @RequestBody @Valid EditUserDTO userAccountDTO
    ) {
        if (userAccount == null) {
            throw new RuntimeException("Not authenticated user can't edit any user account. It can occurs due inpatient refactoring.");
        }

        UserAccount exists = userAccountRepository.findById(userAccount.getId()).orElseThrow(()-> new RuntimeException("Authenticated user account not found in database"));

        // check email already present
        if (!exists.getEmail().equals(userAccountDTO.getEmail()) && userAccountRepository.findByEmail(userAccountDTO.getEmail()).isPresent()) {
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

    // TODO delete user with delete avatar

    @GetMapping(Constants.Uls.SESSIONS+"/my")
    public Map<String, ExpiringSession> mySessions(@AuthenticationPrincipal UserAccountDetailsDTO userDetails){
        return blogUserDetailsService.getMySessions(userDetails);
    }

    @PreAuthorize("@blogSecurityService.hasSessionManagementPermission(#userAccount)")
    @GetMapping(Constants.Uls.SESSIONS)
    public Map<String, ExpiringSession> sessions(@AuthenticationPrincipal UserAccountDetailsDTO userAccount, @RequestParam("userId") long userId){
        return blogUserDetailsService.getSessions(userId);
    }

    @PreAuthorize("@blogSecurityService.hasSessionManagementPermission(#userAccount)")
    @DeleteMapping(Constants.Uls.SESSIONS)
    public void killSessions(@AuthenticationPrincipal UserAccountDetailsDTO userAccount, @RequestParam("userId") long userId){
        blogUserDetailsService.killSessions(userId);
    }

    @PreAuthorize("@blogSecurityService.canLock(#userAccountDetailsDTO)")
    @PostMapping(Constants.Uls.USER + Constants.Uls.LOCK)
    public void setLocked(@AuthenticationPrincipal UserAccountDetailsDTO userAccountDetailsDTO, @RequestBody LockDTO locked){
        UserAccount userAccount = blogUserDetailsService.getUserAccount(locked.getUserId());
        if (locked.isLock()){
            blogUserDetailsService.killSessions(locked.getUserId());
        }
        userAccount.setLocked(locked.isLock());
    }
}