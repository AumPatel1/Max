package org.example.matching.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class WalletShareId implements Serializable {
    private String userId;
    private String instrument;
}
