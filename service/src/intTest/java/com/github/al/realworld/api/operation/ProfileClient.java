package com.github.al.realworld.api.operation;

import com.github.al.realworld.rest.support.LocalFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "profile", path = "${api.version}", configuration = LocalFeignConfig.class)
public interface ProfileClient {
}
