package com.logpose.api.dto;

import com.logpose.api.model.Printing;

public record PrintingDto(
        String sourceId,
        String setCode,
        String setName,
        String rarity,
        String imageUrl,
        Integer blockNumber
) {

    public static PrintingDto from(Printing printing) {
        return new PrintingDto(
                printing.getSourceId(),
                printing.getSetCode(),
                printing.getSetName(),
                printing.getRarity(),
                printing.getImageUrl(),
                printing.getBlockNumber());
    }
}
