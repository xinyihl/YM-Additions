import mods.ymadditions.NetHubPowerUsage;

// 默认计算公式: (powerBase + powerDistanceMultiplier * context * ln(context^2 + 3)) * otherDimMultiplier (AE/t)

// 设置耗电计算函数A
[context 节点间的距离，isOtherDim 是否跨纬度]
NetHubPowerUsage.calcNetHubPowerUsage(function(context as double, isOtherDim as bool){
    return 0.0D;
});

// 设置耗电计算函数B
[thisBlockPos 子节点BlockPos，thatBlockPos 主节点BlockPos，thisDimension 子节点维度id，thatDimension 主节点维度id]
NetHubPowerUsage.calcNetHubPowerUsage(function(thisBlockPos as IBlockPos, thatBlockPos as IBlockPos, thisDimension as int, thatDimension as int){
    return 0.0D;
});