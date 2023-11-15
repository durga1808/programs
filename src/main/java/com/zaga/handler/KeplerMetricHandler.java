package com.zaga.handler;

import com.zaga.repo.KeplerMetricRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class KeplerMetricHandler {

    @Inject 
    KeplerMetricRepo keplerMetricRepo;
}
