package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.dto.EditUserDTO;
import com.github.nikit.cpp.dto.Wrapper;
import com.github.nikit.cpp.entity.jpa.UserAccount;
import com.github.nikit.cpp.exception.UserAlreadyPresentException;
import com.github.nikit.cpp.utils.PageUtils;
import com.github.nikit.cpp.converter.UserAccountConverter;
import com.github.nikit.cpp.dto.UserAccountDTO;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.repo.jpa.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
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
            @RequestParam(value = "size", required=false, defaultValue = "0") int size
    ) {
        PageRequest springDataPage = new PageRequest(PageUtils.fixPage(page), PageUtils.fixSize(size), Sort.Direction.ASC, "id");

        return new Wrapper<>(
                userAccountRepository.findAll(springDataPage).getContent().stream()
                        .map(UserAccountConverter::convertToUserAccountDTO).collect(Collectors.toList()),
                userAccountRepository.count()
        );
    }

    @GetMapping(value = Constants.Uls.USER+Constants.Uls.USER_ID)
    public UserAccountDTO getUser(@PathVariable(Constants.PathVariables.USER_ID) Long userId) {

        return userAccountRepository.findById(userId).map(UserAccountConverter::convertToUserAccountDTO)
                .orElseThrow(() -> new RuntimeException("user with id="+ userId + " not found"));
    }

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
            UserAccountConverter.convertToEditUserDto(exists); // we care for email leak...
        }
        // check login already present
        if (!exists.getUsername().equals(userAccountDTO.getLogin()) && userAccountRepository.findByUsername(userAccountDTO.getLogin()).isPresent()) {
            throw new UserAlreadyPresentException("User with login '" + userAccountDTO.getLogin() + "' is already present");
        }

        UserAccountConverter.updateUserAccountEntity(userAccountDTO, exists, passwordEncoder);
        exists = userAccountRepository.save(exists);

        sendNeedRefreshProfile(httpServletResponse);
        return UserAccountConverter.convertToEditUserDto(exists);
    }

    // will be in utility class in future
    private void sendNeedRefreshProfile(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader(Constants.Headers.NEED_REFRESH_PROFILE, Boolean.TRUE.toString());
        // offered via additional header which will be handled in js http client
    }
}