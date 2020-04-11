package com.boost.upgrades.data.api_model.GetAllFeatures.response

data class Feature(
    val _kid: String,
    val _parentClassId: String,
    val _parentClassName: String,
    val _propertyName: String,
    val boost_widget_key: String,
    val createdon: String,
    val description: String,
    val description_title: String,
    val discount_percent: Int,
    val feature_code: String,
    val is_premium: Boolean,
    val isarchived: Boolean,
    val learn_more_link: LearnMoreLink,
    val name: String,
    val price: Int,
    val primary_image: PrimaryImage?,
    val secondary_images: List<SecondaryImage>?,
    val target_business_usecase: String,
    val time_to_activation: Int,
    val updatedon: String,
    val usecase_importance: String,
    val feature_importance: Int,
    val websiteid: String,
    val feature_banner: FeatureBanner,
    val total_installs: String

)