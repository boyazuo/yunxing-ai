package com.yxboot.modules.account.dto;

import com.yxboot.modules.account.enums.TenantUserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户中的用户信息
 *
 * @author liangliang
 */
@Data
@Schema(description = "租户中的用户信息")
public class UserInTenantDTO {
    @Schema(description = "用户ID")
    private long userId;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "用户角色")
    private TenantUserRole role;
    @Schema(description = "是否活跃用户")
    private boolean isActive;

    /**
     * 使用 lombok isActive 会自动去掉is
     * 手动增加一个get方法
     *
     * @return isActive
     */
    public boolean getIsActive() {
        return isActive;
    }
}
