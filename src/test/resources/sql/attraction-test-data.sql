INSERT INTO contenttypes (id, name) VALUES (12, '관광지'), (14, '문화시설'), (15, '축제공연행사'), (25, '여행코스'), (28, '레포츠'), (32, '숙박'), (38, '쇼핑'), (39, '음식점');

INSERT INTO attractions (id, content_id, title, first_image1, first_image2, map_level, latitude, longitude, tel, addr1, addr2, homepage, overview, content_type_id, sido_code, sigungu_code)
VALUES
(125266, 125266, '국립중앙박물관', 'http://tong.visitkorea.or.kr/cms/resource/84/2686884_image2_1.jpg', 'http://tong.visitkorea.or.kr/cms/resource/84/2686884_image3_1.jpg', 6, 37.5238, 126.9805, '02-2077-9000', '서울특별시 용산구 서빙고로 137', '', '<a href="http://www.museum.go.kr" target="_blank" title="새창 : 국립중앙박물관 홈페이지로 이동">www.museum.go.kr</a>', '대한민국 역사와 문화의 정수를 간직한 곳', 14, 1, 20);

INSERT INTO path_coords (id, latitude, longitude, ordering, course_id) VALUES (1, 37.5238, 126.9805, 1, 1);