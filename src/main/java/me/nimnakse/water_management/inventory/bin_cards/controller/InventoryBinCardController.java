package me.nimnakse.water_management.inventory.bin_cards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.inventory.bin_cards.service.InventoryBinCardService;
import me.nimnakse.water_management.stock_cards.dto.response.BinCardEntryRes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory-bin-cards")
@Tag(name = "Inventory Bin Cards", description = "Inventory bin card reporting")
@CrossOrigin
public class InventoryBinCardController {
    private final InventoryBinCardService binCardService;

    public InventoryBinCardController(InventoryBinCardService binCardService) {
        this.binCardService = binCardService;
    }

    @GetMapping
    @Operation(summary = "Get inventory bin card", description = "Fetches paginated bin card entries for an inventory item.")
    public ResponseEntity<ApiResponse<PageResponse<BinCardEntryRes>>> getBinCard(
            @RequestParam Long inventoryTemplateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(binCardService.getBinCard(inventoryTemplateId, page, size)));
    }
}
