package muni.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String documentId;
    private String hash;
    private String transactionHash;
    private String timestamp;
    private String registeredBy;
    private boolean verified;
}
