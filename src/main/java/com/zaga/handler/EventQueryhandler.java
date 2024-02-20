package com.zaga.handler;

import com.zaga.repo.EventQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventQueryhandler {
    
@Inject
EventQueryRepo eventQueryRepo;


}
