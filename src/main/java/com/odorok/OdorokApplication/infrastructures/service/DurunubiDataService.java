package com.odorok.OdorokApplication.infrastructures.service;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public interface DurunubiDataService {
    // 두루누비 길 데이터 로드
    void loadGilDatas();
    // 두루누비 코스 데이터 로드
    void loadCourseDatas();

    // 두루누비 좌표 데이터 로드
    void loadGPX(String url, Long crsId) throws ParserConfigurationException, SAXException;

    // 데이터베이스에 삽입
    void loadToLocalDB();

    void updateLocalDB();
}
