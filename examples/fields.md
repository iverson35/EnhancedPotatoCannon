### 中文
- affect_radius 爆炸影响范围
- explosion_knockback 爆炸击退(可为负)  
``根据与爆炸中心的距离二次减少(设为负时为增加)``
- explosion_damage 爆炸伤害  
``根据与爆炸中心的距离线性减少``
- penetrate_ratio 爆炸/药水效果的掩体穿透率  
``当弹丸爆炸时，部分或全部待在掩体后的玩家会受到爆炸伤害，击退与药水效果时长的减免，具体减免率取决于玩家暴露在外的部位，减免的比例最大不超过(1-穿透率)``
- effects 直接命中与爆炸时施加的药水效果
- - effect 效果ID
- - duration 效果时长
- - amplifier 效果等级
- max_reflect 最大反射次数
- reflect_decay 每次反射带来的速度衰减  

**不需要的字段可以不用填写。例如不需要反射特性时，max_reflect和reflect_decay无需声明。**
### English
- affect_radius: Explosion effect radius
- explosion_knockback: Explosion knockback (can be negative)  
``Decreases quadratically with distance from the explosion center (increases if set to negative)``
- explosion_damage: Explosion damage
``Decreases linearly with distance from the explosion center``
- penetrate_ratio: Cover penetration rate for explosion/potion effects  
``When a projectile explodes, players partially or fully behind cover receive reduced explosion damage, knockback, and potion effect duration. The reduction depends on exposed body parts and cannot exceed (1 - penetration ratio)``
- effects: Potion effects applied on direct hit and explosion
- - effect: Effect ID
- - duration: Effect duration
- - amplifier: Effect level
- max_reflect: Maximum reflection count
- reflect_decay: Velocity decay per reflection

**You can leave any unnecessary fields blank. For example, if reflection properties are not needed, there is no need to declare max_reflect and reflect_decay**