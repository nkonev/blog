package com.github.nkonev.controllers;

import com.github.nkonev.Constants;
import com.github.nkonev.dto.*;
import com.github.nkonev.entity.jpa.UserAccount;
import com.github.nkonev.exception.UserAlreadyPresentException;
import com.github.nkonev.security.BlogUserDetailsService;
import com.github.nkonev.utils.PageUtils;
import com.github.nkonev.converter.UserAccountConverter;
import com.github.nkonev.repo.jpa.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.util.Collection;
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
    public Wrapper<UserAccountDTO> getUsersGet(
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required=false, defaultValue = "") String searchString
    ) {
        PageRequest springDataPage = new PageRequest(PageUtils.fixPage(page), PageUtils.fixSize(size), Sort.Direction.ASC, "id");

        Page<UserAccount> resultPage = userAccountRepository.findByUsernameContains(springDataPage, searchString);
        return new Wrapper<>(
                resultPage.getContent().stream()
                        .map(UserAccountConverter::convertToUserAccountDTO).collect(Collectors.toList()),
                resultPage.getTotalElements()
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

    @Autowired
    private BlogUserDetailsService blogUserDetailsService;

    @PostMapping(Constants.Uls.PROFILE)
    public EditUserDTO editProfile(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount,
            @RequestBody @Valid EditUserDTO userAccountDTO,
            HttpServletResponse httpServletResponse
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

        sendNeedRefreshProfile(httpServletResponse);
        return UserAccountConverter.convertToEditUserDto(exists);
    }

    // TODO will be in utility class in future
    private void sendNeedRefreshProfile(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader(Constants.Headers.NEED_REFRESH_PROFILE, Boolean.TRUE.toString());
        // offered via additional header which will be handled in js http client
    }

    // TODO delete user with delete avatar
}