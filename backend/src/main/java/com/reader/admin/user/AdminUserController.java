package com.reader.admin.user;

import com.reader.admin.AdminPaging;
import com.reader.common.PageResponse;
import com.reader.common.Result;
import jakarta.validation.Valid;
import java.util.function.Function;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Result<PageResponse<AdminUserResponse>> list(@RequestParam(defaultValue = "") String keyword,
                                                        @RequestParam(defaultValue = "false") boolean deleted,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return Result.ok(PageResponse.of(
                adminUserService.search(keyword.strip(), deleted,
                        AdminPaging.of(deleted, page, size)),
                Function.identity()));
    }

    @PutMapping("/{id}/membership")
    public Result<AdminUserResponse> changeMembership(@PathVariable Long id,
                                                      @Valid @RequestBody MembershipRequest request) {
        return Result.ok(adminUserService.changeMembership(id, request.memberUntil()));
    }

    @DeleteMapping("/{id}")
    public Result<AdminUserResponse> delete(@PathVariable Long id) {
        return Result.ok(adminUserService.delete(id));
    }

    @PostMapping("/{id}/restore")
    public Result<AdminUserResponse> restore(@PathVariable Long id) {
        return Result.ok(adminUserService.restore(id));
    }
}
