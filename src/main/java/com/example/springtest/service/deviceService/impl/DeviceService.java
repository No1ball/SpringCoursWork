package com.example.springtest.service.deviceService.impl;

import com.example.springtest.entity.DevicesSqlDao;

import java.util.List;

public interface DeviceService<T> {
    void getPDF();
    List<DevicesSqlDao> search(String name);
    T addDevice(DevicesSqlDao devices);
    void delDev(int id);
    T putDec(int id, DevicesSqlDao devices);
    List<DevicesSqlDao> getDevices();
}