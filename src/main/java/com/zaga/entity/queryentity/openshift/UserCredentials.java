package com.zaga.entity.queryentity.openshift;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import jakarta.json.bind.annotation.JsonbNillable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("id")
@MongoEntity(collection = "UserCreds", database = "ObservabilityCredentials")
public class UserCredentials extends PanacheMongoEntity {
    private String username;
    private String password;
    @JsonbNillable
    private List<String> roles;
    private List<Environments> environments;
}
