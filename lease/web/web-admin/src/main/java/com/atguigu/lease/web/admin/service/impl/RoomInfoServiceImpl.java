package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private RoomFacilityService roomFacilityService;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;
    private NativeWebRequest nativeWebRequest;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;


    //保存或更新房间信息
    @Override
    public void saveOrUpdateRoomInfo(RoomSubmitVo roomSubmitVo) {
        //获取id，判断是否为空
        boolean isUpdate = roomSubmitVo.getId() != null;
        //调用方法添加或者修改，房间基本信息
        this.saveOrUpdate(roomSubmitVo);

        //如果修改
        if(isUpdate) {
            //删除相关数据
            //1 删除配套数据
            LambdaUpdateWrapper<RoomFacility> wrapper01 = new LambdaUpdateWrapper<>();
            wrapper01.eq(RoomFacility::getRoomId,roomSubmitVo.getId());
            roomFacilityService.remove(wrapper01);

            //2 删除图片
            LambdaUpdateWrapper<GraphInfo> wrapper02 = new LambdaUpdateWrapper<>();
            wrapper02.eq(GraphInfo::getItemId,roomSubmitVo.getId());
            wrapper02.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoService.remove(wrapper02);

            //3.删除原有roomAttrValueList
            LambdaQueryWrapper<RoomAttrValue> attrQueryMapper = new LambdaQueryWrapper<>();
            attrQueryMapper.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(attrQueryMapper);

            //4.删除原有roomLabelList
            LambdaQueryWrapper<RoomLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
            labelQueryWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(labelQueryWrapper);

            //5.删除原有paymentTypeList
            LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
            paymentQueryWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(paymentQueryWrapper);

            //6.删除原有leaseTermList
            LambdaQueryWrapper<RoomLeaseTerm> termQueryWrapper = new LambdaQueryWrapper<>();
            termQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(termQueryWrapper);
        }
        //保存新的graphInfoList
        //从vo获取图片列表集合
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        //判断集合是否为空
        if(!CollectionUtils.isEmpty(graphVoList)) {
            List<GraphInfo> list = new ArrayList<>();
            //graphVoList遍历
            for(GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                //图片名称
                graphInfo.setName(graphVo.getName());
                //类型 ：公寓 房间
                graphInfo.setItemType(ItemType.ROOM);
                // id
                graphInfo.setItemId(roomSubmitVo.getId());
                //图片地址
                graphInfo.setUrl(graphVo.getUrl());
                //放到list集合
                list.add(graphInfo);
            }
            //批量添加
            graphInfoService.saveBatch(list);
        }

        //保存新的roomAttrValueList
        List<Long> attrValueIdList = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(attrValueIdList)) {
            List<RoomAttrValue> list = new ArrayList<>();

            for(Long arrtValueId:attrValueIdList) {

                // RoomAttrValue roomAttrValue1 =
                //  RoomAttrValue.builder().attrValueId(arrtValueId)
                //  .roomId(roomSubmitVo.getId()).build();

                RoomAttrValue roomAttrValue = new RoomAttrValue();
                roomAttrValue.setRoomId(roomSubmitVo.getId());
                roomAttrValue.setAttrValueId(arrtValueId);

                list.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(list);
        }
        //3.保存新的facilityInfoList
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIds)) {
            List<RoomFacility> roomFacilityList = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIds) {
                RoomFacility roomFacility =
                        RoomFacility.builder().roomId(roomSubmitVo.getId())
                                .facilityId(facilityInfoId).build();
                roomFacilityList.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilityList);
        }

        //4.保存新的labelInfoList
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if (!CollectionUtils.isEmpty(labelInfoIds)) {
            ArrayList<RoomLabel> roomLabelList = new ArrayList<>();
            for (Long labelInfoId : labelInfoIds) {
                RoomLabel roomLabel =
                        RoomLabel.builder().roomId(roomSubmitVo.getId())
                                .labelId(labelInfoId).build();
                roomLabelList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelList);
        }

        //5.保存新的paymentTypeList
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypeIds)) {
            ArrayList<RoomPaymentType> roomPaymentTypeList = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType roomPaymentType =
                        RoomPaymentType.builder().roomId(roomSubmitVo.getId())
                                .paymentTypeId(paymentTypeId).build();
                roomPaymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeList);
        }

        //6.保存新的leaseTermList
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermIds)) {
            ArrayList<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for (Long leaseTermId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm =
                        RoomLeaseTerm.builder().roomId(roomSubmitVo.getId())
                                .leaseTermId(leaseTermId).build();
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }
    }

    //根据条件分页查询房间列表
    @Override
    public IPage<RoomItemVo> selectRoomInfoPage(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.selectRoomInfoPage(page,queryVo);
    }

    //根据id获取房间详细信息
    @Override
    public RoomDetailVo getDetailById(Long id) {
        RoomInfo roomInfo = this.getById(id);

        if(roomInfo == null){
            return null;
        }

        //根据公寓id获取公寓信息
        Long apartmentId = roomInfo.getApartmentId();
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(apartmentId);

        //根据房间id获取配套数据
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.findFacilityListByRoomId(id);

        //根据 id获取图片部分
        List<GraphVo> graphVoList = graphInfoMapper.selectGraphListByRoomId(ItemType.ROOM,id);

        //其他获取代码
        //4.查询attrValueList
        List<AttrValueVo> attrvalueVoList = attrValueMapper.selectListByRoomId(id);

        //6.查询labelInfoList
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        //7.查询paymentTypeList
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

        //8.查询leaseTermList
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);

        RoomDetailVo adminRoomDetailVo=new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo,adminRoomDetailVo);

        adminRoomDetailVo.setApartmentInfo(apartmentInfo);
        adminRoomDetailVo.setGraphVoList(graphVoList);
        adminRoomDetailVo.setAttrValueVoList(attrvalueVoList);
        adminRoomDetailVo.setFacilityInfoList(facilityInfoList);
        adminRoomDetailVo.setLabelInfoList(labelInfoList);
        adminRoomDetailVo.setPaymentTypeList(paymentTypeList);
        adminRoomDetailVo.setLeaseTermList(leaseTermList);

        return adminRoomDetailVo;
    }
}




