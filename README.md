# TFC Smithing Skill — TerraFirmaCraft 锻造技艺系统

为 TerraFirmaCraft 4.1.2 (NeoForge 1.21.1) 添加 TFC+ 风格的锻造技艺系统。

Adds a TFC+-style smithing skill system for TerraFirmaCraft 4.1.2 (NeoForge 1.21.1).

---

## 依赖 / Dependencies

| Mod | Type | Description |
|-----|------|-------------|
| TerraFirmaCraft 4.1.2 | compile | Core mod |
| Patchouli | compile | TFC handbook |

---

## 技术概览 / Technical Overview

### 四个技艺 / Four Skills

| Skill | ID | XP Rate | Match Pattern |
|-------|-----|---------|---------------|
| General Smithing | `gensmith` | 250 | All forging (all-purpose) |
| Tool Smithing | `toolsmith` | 100 | `_head` / `_blade` suffix (non-weapon, non-armor) |
| Weapon Smithing | `weaponsmith` | 100 | `sword_blade` / `mace_head` / `javelin_head` / `knife_blade` |
| Armor Smithing | `armorsmith` | 100 | `unfinished_helmet` / `chestplate` / `greaves` / `boots` / `_shield` |

Specialist determination resolves via registered rules: built-in descriptionId keyword matching, config-driven exact item ID matching, and programmatic `Predicate<ItemStack>` registration by other mods.

### 公式 / Formulas

```
baseMult    = 1 - rate / (rate + XP)                    // [0, 1.0]
skillMult   = base + baseMult × range                    // [0.5, 1.5]

rankBonus   = Novice 0% / Adept 20% / Expert 40% / Master 70%
xpBonus     = tier progress × max_per_tier               // [0, 10%]
skillExtra  = rankBonus + xpBonus                        // [0, 80%]
```

Forging uses weighted average; plain plates/welding use gensmith-only:

```
weightedSkillMult  = 0.7 × gensmith_mult  + 0.3 × specialist_mult
weightedSkillExtra = 0.7 × gensmith_extra + 0.3 × specialist_extra
```

### 品质门槛修正 / Quality Threshold

```
effectiveRatio = ratio / skillMult
→ Higher skill → fewer steps needed for high quality
```

### 额外加成 / Bonus Attributes

| Dimension | Hook | Formula |
|-----------|------|---------|
| Mining speed | `PlayerEvent.BreakSpeed` | `speed × (1 + skillExtra)` |
| Attack damage | `LivingIncomingDamageEvent` | `damage × (1 + skillExtra)` |
| Max durability | `DataComponents.MAX_DAMAGE` | `maxDmg × (1 + skillExtra)` |
| Attack tooltip | `ItemAttributeModifierEvent` | Shows modifier in tooltip |
| Armor | Extra durability only | Filtered by `SKILL_TYPE` |

### Skill Component Transfer

| Operation | Behavior |
|-----------|----------|
| Forging complete | Newly applied |
| Crafting table | Copy best (input → output) |
| Welding | Copy best (higher skillExtra of two inputs) |

### 经验授予 / XP Granting

XP is granted on **every** anvil recipe completion — not limited to tool/weapon/armor recipes.

- `gensmith` XP is always granted on forge complete (1 XP per completion).
- Specialist XP (toolsmith / weaponsmith / armorsmith) is granted in addition when the output matches a specialist rule.
- A forge is detected as complete when **either**: the `ForgingCapability` is cleared on the result, **or** the item type in slot 0 has changed (catches intermediate products like sheets that retain forging capability for further working).

---

## GUI

- **Anvil**: Gold text `Skill: 1.27 (Master)` at `(leftPos+105, topPos+6)`, 0.7x scale
- **Tooltip**: TFC quality line replaced to `Well Forged by [T.S. Expert] Dev`; Master rank line uses `LIGHT_PURPLE` (no italic); extra line `Skill: +45.0%` in gold `0xD0A000` (uniform across all ranks)
- **Skill Tab**: Anvil icon button on inventory right side → opens SkillScreen with 4 skill progress bars, rank, skillMult, skillExtra

---

## 调试命令 / Debug Command (`/tfcskill`)

Requires permission level 2.

| Command | Description | Example |
|---------|-------------|---------|
| `get` | Show all skills | `/tfcskill get` |
| `get <skill>` | Show single skill | `/tfcskill get gensmith` |
| `add <skill> <n>` | Add XP | `/tfcskill add toolsmith 50` |
| `set <skill> <n>` | Set XP | `/tfcskill set gensmith 0` |
| `rankset <skill> <rank>` | Set rank | `/tfcskill rankset gensmith expert` |

Rank values: `novice` / `adept` / `expert` / `master`

---

## 配置 / Configuration

配置文件位置 / Config path: `run/config/tfcs-server.toml`

```toml
[skill_mult]
base = 0.5          # skillMult lower bound
range = 1.0         # skillMult upper bound = base + range

[rank_bonus]
novice = 0.00
adept  = 0.20
expert = 0.40
master = 0.70

[xp_bonus]
max_per_tier = 0.10  # Max XP bonus within a tier

[weight]
gensmith = 0.7       # Weight of General Smithing (specialist = 1 - this)

[xp_rates]
gensmith    = 250
toolsmith   = 100
weaponsmith = 100
armorsmith  = 100

[toggles]
modify_ratio      = true   # Skill affects quality threshold
apply_skill_extra = true   # Bonus attributes take effect
grant_xp          = true   # Grant XP on forge complete
```

### 跨 Mod 物品映射 / Cross-Mod Item Mappings

Integrate items from other mods by adding them to `[skill_mappings]`:

```toml
[skill_mappings]
toolsmith   = ["othermod:pickaxe_head", "othermod:axe_blade", "thirdmod:hammer_head"]
weaponsmith = ["othermod:sword_blade",  "othermod:dagger_blade"]
armorsmith  = ["othermod:helmet_plate", "othermod:chestplate_part"]
```

These items will be auto-detected during TFC anvil forging with full skill bonus application.

---

## 公共 API / Public API

Package: `com.nbw.tfc.api`

Add to your `build.gradle`:

```groovy
dependencies {
    implementation files("libs/tfcsmithingskill-x.x.x.jar")
}
```

### 方法总览 / Method Overview

```java
import com.nbw.tfc.api.TFCSewingAPI;
import com.nbw.tfc.skill.SkillDef;

// ========== 技艺判定 / Skill Type Determination ==========

// Determine which skill category an ItemStack belongs to (returns SkillDef or null for General-only)
SkillDef skill = TFCSewingAPI.determineSkillType(ItemStack stack);

// Check if an item already has skill bonuses applied
boolean hasSkill = TFCSewingAPI.hasSkillData(ItemStack stack);

// ========== 数值计算 / Value Calculation ==========

// Calculate weighted skillMult for a player (considers specialist + gensmith)
// specialist=null → uses gensmith-only
float mult = TFCSewingAPI.calculateSkillMult(ServerPlayer player, SkillDef specialist);
float mult = TFCSewingAPI.calculateSkillMult(ServerPlayer player, ItemStack item);

// Calculate weighted skillExtra for a player
float extra = TFCSewingAPI.calculateSkillExtra(ServerPlayer player, SkillDef specialist);
float extra = TFCSewingAPI.calculateSkillExtra(ServerPlayer player, ItemStack item);

// Calculate rank name string ("Novice" / "Adept" / "Expert" / "Master")
String rank = TFCSewingAPI.calculateSkillRank(ServerPlayer player, SkillDef specialist);

// ========== 写入物品 / Apply to ItemStack ==========

// Compute bonuses from player data AND apply all skill components to an ItemStack
// (SKILL_EXTRA, SKILL_RANK, SKILL_TYPE + MAX_DAMAGE if damageable)
TFCSewingAPI.applySkillBonuses(ItemStack stack, ServerPlayer player, SkillDef specialist);
// specialist can be null → will use gensmith-only calculation

// Apply pre-computed skill values directly (useful when values are already calculated)
TFCSewingAPI.applySkillValues(ItemStack stack, float skillExtra, String rank, SkillDef skillType);

// ========== 经验奖励 / XP Management ==========

// Grant XP to a player (1 gensmith XP + 1 specialist XP if specialist != null)
TFCSewingAPI.grantXp(ServerPlayer player, SkillDef specialist);

// Get raw SkillData attachment for a player (read/write XP values directly)
SkillData data = TFCSewingAPI.getSkillData(ServerPlayer player);

// ========== 注册规则 / Register Custom Rules ==========

// Register a custom item-to-skill matching rule
TFCSewingAPI.registerItemRule(
    ResourceLocation.fromNamespaceAndPath("mymod", "my_sword"),
    stack -> stack.is(MyModItems.MY_SWORD_BLADE.get()),
    SkillDef.WEAPONSMITH
);

// Remove a previously registered rule
TFCSewingAPI.unregisterItemRule(myRuleId);

// ========== 工具 / Utilities ==========

// Get the mod ID ("tfcsmithingskill")
String modId = TFCSewingAPI.getModId();
```

### 完整使用示例 / Full Usage Example

```java
// For other mod developers: call this after TFC anvil forging completes in your mod
public static void onTfcAnvilFinish(ServerPlayer player, ItemStack forgedItem) {
    // 1. Determine the specialist skill for the forged item
    //    (uses built-in + config + registered rules)
    SkillDef specialist = TFCSewingAPI.determineSkillType(forgedItem);

    // 2. Apply all skill bonuses (extra, rank, type, max_damage)
    TFCSewingAPI.applySkillBonuses(forgedItem, player, specialist);

    // 3. Grant XP
    TFCSewingAPI.grantXp(player, specialist);
}
```

### 数据组件说明 / Data Components

| Component | Type | Values |
|-----------|------|--------|
| `tfcsmithingskill:skill_extra` | Float | `[0, 0.8]` weighted skill bonus |
| `tfcsmithingskill:skill_rank` | String | `Novice` / `Adept` / `Expert` / `Master` |
| `tfcsmithingskill:skill_type` | String? | Translation key (e.g. `skill.tfcsmithingskill.weaponsmith`) or null |
| `minecraft:max_damage` | Int | Original × `(1 + skillExtra)` |

---

## 数据存储 / Data Storage

- **Player → AttachmentType**: `SkillData` (NeoForge `INBTSerializable`) stored in player NBT, synced via `SyncSkillPacket`
- **ItemStack → DataComponent**: See table above

---

## 文件结构 / File Structure

```
src/main/java/com/nbw/tfc/
├── TFCSmithingSkill.java              # Main entry: registration + config init
├── TFCSmithingSkillClient.java        # Client @Mod entry
├── api/
│   ├── TFCSewingAPI.java              # Public API for other mods
│   └── ItemSkillRules.java            # Shared item→skill rule registry
├── client/
│   ├── ClientGameEvents.java          # Anvil GUI + inventory button + tooltip
│   └── SkillTabButton.java            # Inventory skill tab button
├── command/
│   └── SkillCommand.java              # /tfcskill debug commands
├── event/
│   ├── PlayerEvents.java              # Login/respawn skill sync
│   └── SkillBonusEvents.java          # BreakSpeed / LivingHurt / Craft / Attribute
├── mixin/
│   ├── AnvilBlockEntityMixin.java     # Ratio fix + forge-complete detect (item-type) + XP + bonus + weld copy
│   └── ItemStackMixin.java            # Item serialization
└── skill/
    ├── SkillAttachments.java          # NeoForge AttachmentType registration
    ├── SkillComponents.java           # DataComponentType registration
    ├── SkillData.java                 # Player skill data (INBTSerializable)
    ├── SkillDef.java                  # Skill enum (4 types)
    ├── Skills.java                    # All formulas (skillMult / skillExtra / weighted)
    ├── config/
    │   └── ServerConfig.java          # Configurable parameters + skill_mappings
    ├── network/
    │   └── SyncSkillPacket.java       # Server→client skill sync packet
    └── screen/
        └── SkillScreen.java           # Skill tab GUI
```

### Mixin Injection Points

| Mixin | Method | Type | Purpose |
|-------|--------|------|---------|
| `AnvilBlockEntityMixin` | `work()` | `@Inject(HEAD)` | Capture input item type + forging capability for completion detection |
| `AnvilBlockEntityMixin` | `work()` | `@ModifyVariable` | `ratio = ratio / skillMult` |
| `AnvilBlockEntityMixin` | `work()` | `@Inject(RETURN)` | Detect forge complete (capability cleared **OR** item type changed) → XP grant + skill bonus + MAX_DAMAGE |
| `AnvilBlockEntityMixin` | `weld()` | `@Inject(HEAD)` | Capture weld input skill components |
| `AnvilBlockEntityMixin` | `weld()` | `@Inject(RETURN)` | Copy best skill component to weld output |

---

## 已知限制 / Known Limitations

- **Durability mixin unimplemented**: Only flat max durability boost; no probabilistic durability saving (requires `@WrapOperation(priority=1001)` with MixinExtras refMap support)
- **No refMap**: External jar field/method names cannot be resolved via refMap; uses reflection + `@ModifyVariable(ordinal)` instead
