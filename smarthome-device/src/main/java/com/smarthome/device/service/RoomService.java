package com.smarthome.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.model.entity.Room;
import com.smarthome.model.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 房间管理服务
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomMapper roomMapper;

    public Room addRoom(Room room) {
        roomMapper.insert(room);
        return room;
    }

    public void updateRoom(Room room) {
        roomMapper.updateById(room);
    }

    public void deleteRoom(Long roomId) {
        roomMapper.deleteById(roomId);
    }

    public Room getById(Long roomId) {
        return roomMapper.selectById(roomId);
    }

    public List<Room> listAll() {
        return roomMapper.selectList(
                new LambdaQueryWrapper<Room>().orderByAsc(Room::getFloor).orderByAsc(Room::getSortOrder)
        );
    }
}
