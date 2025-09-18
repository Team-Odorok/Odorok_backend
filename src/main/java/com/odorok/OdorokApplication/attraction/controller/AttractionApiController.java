package com.odorok.OdorokApplication.attraction.controller;

import com.odorok.OdorokApplication.attraction.dto.response.holder.AttractionResponse;
import com.odorok.OdorokApplication.attraction.dto.response.holder.ContentTypeResponse;
import com.odorok.OdorokApplication.attraction.dto.response.item.AttractionDetail;
import com.odorok.OdorokApplication.attraction.service.AttractionQueryService;
import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionApiController {
    private final AttractionQueryService attractionQueryService;

    @GetMapping("/contenttypes")
    public ResponseEntity<ResponseRoot<ContentTypeResponse>> getAllContentTypes() {
        log.debug("Request to /api/attractions/contenttypes");
        ResponseEntity<ResponseRoot<ContentTypeResponse>> response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                .body(CommonResponseBuilder.success("컨텐츠 타입 아이디 조회 성공",
                        new ContentTypeResponse(attractionQueryService.queryAllContentTypes())));
        log.debug("Response from /api/attractions/contenttypes: {}", response.getBody());
        return response;
    }

    @GetMapping("/region")
    public ResponseEntity<ResponseRoot<AttractionResponse>> getRegionalAttractions(
            @RequestParam("sidoCode") Integer sidoCode,
            @RequestParam("sigunguCode") Integer sigunguCode,
            @RequestParam("contentTypeId") Integer contentTypeId) {
        log.debug("Request to /api/attractions/region with sidoCode: {}, sigunguCode: {}, contentTypeId: {}", sidoCode, sigunguCode, contentTypeId);
        ResponseEntity<ResponseRoot<AttractionResponse>> response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                .body(CommonResponseBuilder.success("지역 명소 조회 성공",
                        new AttractionResponse(attractionQueryService.queryRegionalAttractions(
                                sidoCode, sigunguCode, contentTypeId
                        ))));
        log.debug("Response from /api/attractions/region: {}", response.getBody());
        return response;
    }

    @GetMapping("/detail")
    public ResponseEntity<ResponseRoot<AttractionDetail>> getAttractionDetail(
            @RequestParam("attractionId") Long id
    ) {
        log.debug("Request to /api/attractions/detail with attractionId: {}", id);
        try {
            ResponseEntity<ResponseRoot<AttractionDetail>> response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseBuilder.success("명소 상세 조회 성공", attractionQueryService.queryAttractionDetail(id)));
            log.debug("Response from /api/attractions/detail: {}", response.getBody());
            return response;
        } catch(IllegalArgumentException e) {
            log.debug("Exception in /api/attractions/detail: {}", e.getMessage());
            ResponseEntity<ResponseRoot<AttractionDetail>> response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseBuilder.fail(e.getMessage()));
            log.debug("Error response from /api/attractions/detail: {}", response.getBody());
            return response;
        }
    }
}
