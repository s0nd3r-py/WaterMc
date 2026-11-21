package me.s0nd3r.watermc.region.utils;

import abomination.IRegionFile;

import java.io.IOException;

@FunctionalInterface
public interface IRegionCreateFunction {
    IRegionFile create(RegionCreatorInfo info) throws IOException;
}