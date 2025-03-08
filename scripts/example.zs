import mods.ymadditions.NetHubPowerUsage;

// 设置耗电计算函数 [context 为节点间的距离，isOtherDim 为是否跨纬度]
// 默认计算公式: (powerBase + powerDistanceMultiplier * context * ln(context^2 + 3)) * otherBimMultiplier (AE/t)
NetHubPowerUsage.calcNetHubPowerUsage(function(context as double, isOtherDim as bool){
    return 0.0D;
});