rootProject.name = "intellij-autodev"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("experiment")
include("plugin")

include(
    "java"
)
