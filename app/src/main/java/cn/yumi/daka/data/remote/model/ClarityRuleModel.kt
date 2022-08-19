package  cn.yumi.daka.data.remote.model

import java.io.Serializable

class ClarityRuleModel(
    val rule: MutableList<Rule>,
    val timeApi: String,
    val defaultId: String,
    val userFirst: String,
    val episodeChangeClarity: String
) : Serializable {
    class Rule(
        val min: String,
        val max: String,
        val clarityId: String,
        val type: String
    ) : Serializable
}