import mods.ymadditions.NetHubPowerUsage;

// 设置耗电计算函数 [context 节点间的距离，isOtherDim 是否跨纬度]
// 默认计算公式: (powerBase + powerDistanceMultiplier * context * ln(context^2 + 3)) * otherDimMultiplier (AE/t)
NetHubPowerUsage.calcNetHubPowerUsage(function(context as double, isOtherDim as bool){
    return 0.0D;
});

// [thisBlockPos 子节点BlockPos，thatBlockPos 主节点BlockPos，thisDimension 子节点维度id，thatDimension 主节点维度id]
NetHubPowerUsage.calcNetHubPowerUsage(function(IBlockPos thisBlockPos, IBlockPos thatBlockPos, int thisDimension, int thatDimension){
    return 0.0D;
});