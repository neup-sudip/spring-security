package com.example.security.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ActivityProperty {
    private String ip;
    private String agent;
    private String uri;
    private String method;
}
