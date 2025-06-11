package com.viditnakhawa.myimageapp.data

/**
 * The types of configuration editors available.
 */
enum class ConfigEditorType {
    LABEL,
    NUMBER_SLIDER,
    BOOLEAN_SWITCH,
    DROPDOWN,
}

/**
 * The data types of configuration values.
 */
enum class ValueType {
    INT,
    FLOAT,
    DOUBLE,
    STRING,
    BOOLEAN,
}

/**
 * Base class for configuration settings.
 */
open class Config(
    val type: ConfigEditorType,
    open val key: ConfigKey,
    open val defaultValue: Any,
    open val valueType: ValueType,
    open val needReinitialization: Boolean = true,
)

/**
 * Configuration setting for a label.
 */
class LabelConfig(
    override val key: ConfigKey,
    override val defaultValue: String = "",
) : Config(
    type = ConfigEditorType.LABEL,
    key = key,
    defaultValue = defaultValue,
    valueType = ValueType.STRING
)

/**
 * Configuration setting for a number slider.
 */
class NumberSliderConfig(
    override val key: ConfigKey,
    val sliderMin: Float,
    val sliderMax: Float,
    override val defaultValue: Float,
    override val valueType: ValueType,
    override val needReinitialization: Boolean = true,
) :
    Config(
        type = ConfigEditorType.NUMBER_SLIDER,
        key = key,
        defaultValue = defaultValue,
        valueType = valueType,
    )

/**
 * Configuration setting for a boolean switch.
 */
class BooleanSwitchConfig(
    override val key: ConfigKey,
    override val defaultValue: Boolean,
    override val needReinitialization: Boolean = true,
) : Config(
    type = ConfigEditorType.BOOLEAN_SWITCH,
    key = key,
    defaultValue = defaultValue,
    valueType = ValueType.BOOLEAN,
)

/**
 * Configuration setting for a dropdown.
 */
class SegmentedButtonConfig(
    override val key: ConfigKey,
    override val defaultValue: String,
    val options: List<String>,
    val allowMultiple: Boolean = false,
) : Config(
    type = ConfigEditorType.DROPDOWN,
    key = key,
    defaultValue = defaultValue,
    valueType = ValueType.STRING,
)