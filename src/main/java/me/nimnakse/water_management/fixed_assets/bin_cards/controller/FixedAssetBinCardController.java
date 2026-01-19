package me.nimnakse.water_management.fixed_assets.bin_cards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.fixed_assets.bin_cards.service.FixedAssetBinCardService;
import me.nimnakse.water_management.stock_cards.dto.response.BinCardEntryRes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fixed-asset-bin-cards")
@Tag(name = "Fixed Asset Bin Cards", description = "Fixed asset bin card reporting")
@CrossOrigin
public class FixedAssetBinCardController {
    private final FixedAssetBinCardService binCardService;

    public FixedAssetBinCardController(FixedAssetBinCardService binCardService) {
        this.binCardService = binCardService;
    }

    @GetMapping
    @Operation(summary = "Get fixed asset bin card", description = "Fetches paginated bin card entries for a fixed asset.")
    public ResponseEntity<ApiResponse<PageResponse<BinCardEntryRes>>> getBinCard(
            @RequestParam Long fixedAssetTemplateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(binCardService.getBinCard(fixedAssetTemplateId, page, size)));
    }
}
