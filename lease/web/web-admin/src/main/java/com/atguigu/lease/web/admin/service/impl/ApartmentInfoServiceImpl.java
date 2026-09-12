package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private ApartmentFacilityService apartmentFacilityService;
    @Autowired
    private ApartmentLabelService apartmentLabelService;
    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;


    //根据ID获取公寓详细信息
    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        ApartmentInfo apartmentInfo = this.getById(id);
        if (apartmentInfo==null){
            return null;
        }
        //2 根据公寓id查询公寓配套数据
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.findFacilityListByApartmentId(id);

        //3.根据公寓id查询标签数据
        List<LabelInfo> labelInfoList=labelInfoMapper.findLabelListByApartmentId(id);

        //4 根据公寓id查询杂费数据
        List<FeeValueVo> feeValueVoList=feeValueMapper.findFeeValueListByApartmentId(id);

        //5 根据公寓id查询图片数据
        List<GraphVo> graphVoList=graphInfoMapper.selectGraphListByApartmentId(ItemType.APARTMENT,id);

        //6 把上面查询出来所有数据封装到ApartmentDetailVo对象
        ApartmentDetailVo apartmentDetailVo=new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        apartmentDetailVo.setGraphVoList(graphVoList);

        return apartmentDetailVo;
    }

    //根据条件分页查询公寓列表
    @Override
    public IPage<ApartmentItemVo> selectApartmentInfoPage(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        IPage<ApartmentItemVo> pageModel = apartmentInfoMapper.selectApartmentInfoPage(page,queryVo);
        return pageModel;
    }

    //根据id删除公寓信息
    @Override
    public void removeApartmentInfo(Long id) {

        //判断如果公寓下面有房间，不能直接删除公寓  room_info
        //根据公寓id查询room_info表，看公寓在这个表是否存在房间信息
        LambdaQueryWrapper<RoomInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomInfo::getApartmentId,id);
        //select count(*) from room_info where apartment_id=911
        long count = roomInfoMapper.selectCount(wrapper);
        if(count > 0) { //存在房间信息
            throw new RuntimeException("存在房间信息，不能删除");
        }

        //删除公寓基本信息
        this.removeById(id);

        //删除公寓配套数据
        LambdaQueryWrapper<ApartmentFacility> wrapper01 = new LambdaQueryWrapper<>();
        wrapper01.eq(ApartmentFacility::getApartmentId,id);
        apartmentFacilityService.remove(wrapper01);

        //删除ApartmentLabel
        LambdaQueryWrapper<ApartmentLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(ApartmentLabel::getApartmentId, id);
        apartmentLabelService.remove(labelQueryWrapper);

        //删除ApartmentFeeValue
        LambdaQueryWrapper<ApartmentFeeValue> feeQueryWrapper = new LambdaQueryWrapper<>();
        feeQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        apartmentFeeValueService.remove(feeQueryWrapper);

        //删除GraphInfo
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphQueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphQueryWrapper);
    }

    //保存或更新公寓信息
    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        //判断是否进行修改操作
        //判断apartmentSubmitVo是否有id
        boolean isUpdate = apartmentSubmitVo.getId()!=null;

        //1 添加公寓基本数据到 apartment_info:公寓基本信息表
        //apartmentInfoMapper.insert(apartmentSubmitVo);
        this.saveOrUpdate(apartmentSubmitVo);

        if (isUpdate){
            //根据公寓id删除配套、杂费、标签、图片信息
            //delete from apartment_facility where apartment_id=1;
            LambdaQueryWrapper<ApartmentFacility> wrapperApartmentFacility=new LambdaQueryWrapper();
            wrapperApartmentFacility.eq(ApartmentFacility::getApartmentId,apartmentSubmitVo.getId());
            apartmentFacilityService.remove(wrapperApartmentFacility);

            //2.删除公寓杂费数据
            //delete from apartment_fee_value where apartment_id=1;
            LambdaQueryWrapper<ApartmentFeeValue> wrapperApartmentFeeValue=new LambdaQueryWrapper<>();
            wrapperApartmentFeeValue.eq(ApartmentFeeValue::getApartmentId,apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(wrapperApartmentFeeValue);

            //3 删除公寓标签数据
            //DELETE FROM apartment_label  WHERE apartment_id=1
            LambdaQueryWrapper<ApartmentLabel> wrapperApartmentLabel = new LambdaQueryWrapper<>();
            wrapperApartmentLabel.eq(ApartmentLabel::getApartmentId, apartmentSubmitVo.getId());
            apartmentLabelService.remove(wrapperApartmentLabel);

            //4 删除图片数据
            //DELETE FROM graph_info  WHERE item_id=1 and item_type=1
            LambdaQueryWrapper<GraphInfo> wrapperGraphInfo=new LambdaQueryWrapper<>();
            wrapperGraphInfo.eq(GraphInfo::getItemId,apartmentSubmitVo.getId());
            wrapperGraphInfo.eq(GraphInfo::getItemType,ItemType.APARTMENT);
            graphInfoService.remove(wrapperGraphInfo);
        }

        //2 添加公寓配套数据 apartment_facility:公寓配套信息数
        //一个公寓d对应多个配套数据，添加多条记录
        //获取公寓配套数据
        List<Long> facilityInfoIds = apartmentSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIds)) {
//            for (Long fid:facilityInfoIds){
//                //创建apartmentFacility对象，向设置需要添加值
//                ApartmentFacility apartmentFacility=new ApartmentFacility();
//                //设置配套数据id
//                apartmentFacility.setFacilityId(fid);
//                //设置公寓id
//                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
//                apartmentFacilityService.save(apartmentFacility);
//            }
            List<ApartmentFacility> afList=new ArrayList<>();
            for (Long fid:facilityInfoIds){
                //创建apartmentFacility对象，向设置需要添加值
                ApartmentFacility apartmentFacility=new ApartmentFacility();
                //设置配套数据id
                apartmentFacility.setFacilityId(fid);
                //设置公寓id
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                //放到集合
                afList.add(apartmentFacility);
            }
            //调用service有批量添加方法
            apartmentFacilityService.saveBatch(afList);
        }

        //3 添加公寓的标签数据 apartment_label:公寓标签数据
        // 一个公寓id 对应多个标签数据，添加多条记录
        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        if (!CollectionUtils.isEmpty(labelIds)){
        List<ApartmentLabel> list=new ArrayList<>();
            for(Long id:labelIds){
                ApartmentLabel apartmentLabel=new ApartmentLabel();
                apartmentLabel.setLabelId(id);
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                list.add(apartmentLabel);
            }
            apartmentLabelService.saveBatch(list);
        }

        //4 添加公寓杂费数据 apartment_fee_value:公寓杂费数据
        //一个公寓id对应多个杂费数据，添加多条记录
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if (!CollectionUtils.isEmpty(feeValueIds)){
            List<ApartmentFeeValue> list=new ArrayList<>();
            for (Long id:feeValueIds){
                ApartmentFeeValue apartmentFeeValue=new ApartmentFeeValue();
                apartmentFeeValue.setFeeValueId(id);
                apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());

                    list.add(apartmentFeeValue);
            }
            apartmentFeeValueService.saveBatch(list);
        }

        //5 添加公寓图片数据 graph_info:图片表graph_info
        // 添加多条记录添加图片类型(公寓图片还是房间图片)，公寓id
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            List<GraphInfo>list=new ArrayList<>();
            for (GraphVo graphVo:graphVoList){
                GraphInfo graphInfo = new GraphInfo();
                //类型公寓
                graphInfo.setItemType(ItemType.APARTMENT);
                //公寓id
                graphInfo.setItemId(apartmentSubmitVo.getId());
                //图片名称
                graphInfo.setName(graphVo.getName());
                //图片地址
                graphInfo.setUrl(graphVo.getUrl());
                list.add(graphInfo);
            }
            graphInfoService.saveBatch(list);
        }
    }
}




