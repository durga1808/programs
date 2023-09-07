package com.zaga.entity.otellog;

import com.zaga.entity.otellog.resource.Attributes;

import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

import lombok.Data;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    private Attributes attributes; 
}
