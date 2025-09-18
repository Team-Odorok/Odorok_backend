package com.odorok.OdorokApplication.attraction.service;

import com.odorok.OdorokApplication.attraction.dto.response.item.AttractionDetail;
import com.odorok.OdorokApplication.attraction.dto.response.item.AttractionSummary;
import com.odorok.OdorokApplication.attraction.dto.response.item.ContentTypeSummary;

import java.util.List;

public interface AttractionQueryService {
    List<ContentTypeSummary> queryAllContentTypes();
    List<AttractionSummary> queryRegionalAttractions(Integer sidoCode, Integer sigunguCode, Integer ContentTypeId);
    AttractionDetail queryAttractionDetail(Long id);
    List<AttractionSummary> queryCourseCloseAttractions(Long courseId, Integer contentTypeId);
}
