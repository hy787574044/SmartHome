package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.device.service.RoomService;
import com.smarthome.model.entity.Room;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房间管理 API
 */
@Tag(name = "房间管理")
@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "添加房间")
    @PostMapping
    public R<Room> add(@RequestBody Room room) {
        return R.ok(roomService.addRoom(room));
    }

    @Operation(summary = "更新房间")
    @PutMapping
    public R<Void> update(@RequestBody Room room) {
        roomService.updateRoom(room);
        return R.ok();
    }

    @Operation(summary = "删除房间")
    @DeleteMapping("/{roomId}")
    public R<Void> delete(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return R.ok();
    }

    @Operation(summary = "获取房间详情")
    @GetMapping("/{roomId}")
    public R<Room> getById(@PathVariable Long roomId) {
        return R.ok(roomService.getById(roomId));
    }

    @Operation(summary = "获取所有房间")
    @GetMapping("/list")
    public R<List<Room>> listAll() {
        return R.ok(roomService.listAll());
    }
}
