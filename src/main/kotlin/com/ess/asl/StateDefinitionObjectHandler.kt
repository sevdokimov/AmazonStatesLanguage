package com.ess.asl

import com.ess.asl.Field.Companion.field
import com.ess.asl.Field.Companion.fieldN
import com.ess.asl.Field.Companion.fieldP
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.util.containers.PeekableIterator
import com.intellij.util.containers.PeekableIteratorWrapper
import com.jetbrains.jsonSchema.impl.JsonSchemaType

class StateDefinitionObjectHandler : SchemaValueHandler() {

    private val states: Map<String, ObjectValueHandler>
    private val noTypeObject: ObjectValueHandler

    init {
        val comment = field("Comment", StringValueHandler.INSTANCE)
        val queryLang = field("QueryLanguage", StringValueHandler.of(AslUtils.JSON_PATH, AslUtils.JSONata))

        val stateTypes = arrayOf("Task", "Parallel", "Map", "Pass", "Wait", "Choice", "Succeed", "Fail")

        val type = field("Type", StringValueHandler.of(*stateTypes))

        val inputDataP = fieldP("InputPath", JsonPathLanguageInjector.INSTANCE)
        val outputDataP = fieldP("OutputPath", JsonPathLanguageInjector.INSTANCE)
        val assign = field("Assign", TypedValueHandler.OBJECT)
        val next = field("Next", StateReferenceValueHandler())
        val end = field("End", TypedValueHandler(JsonSchemaType._boolean, "true"))
        val resultPathP = fieldP("ResultPath", JsonPathLanguageInjector.REFERENCE_PATH)
        val paramP = fieldP("Parameters", PayloadTemplateValueHandler())
        val resultSelectorP = fieldP("ResultSelector", PayloadTemplateValueHandler())

        val errorListFieldValue = ArrayValueHandler(
            StringValueHandler.of(
                "ALL", "States.HeartbeatTimeout", "States.Timeout", "States.TaskFailed", "States.Permissions",
                "States.ResultPathMatchFailure", "States.ParameterPathFailure", "States.QueryEvaluationError", "States.BranchFailed",
                "States.NoChoiceMatched", "States.IntrinsicFailure", "States.ExceedToleratedFailureThreshold",
                "States.ItemReaderFailed", "States.ResultWriterFailed"
            )
        )

        val outputN = fieldN("Output", StringValueHandler.INSTANCE)
        val argumentsN = fieldN("Arguments", TypedValueHandler.OBJECT)

        val retry = field("Retry", ArrayValueHandler(
            ObjectValueHandler(
                field("ErrorEquals", errorListFieldValue),
                field("IntervalSeconds", TypedValueHandler.INT),
                field("MaxAttempts", TypedValueHandler.INT),
                field("MaxDelaySeconds", TypedValueHandler.INT),
                field("JitterStrategy", StringValueHandler.INSTANCE),
                field("BackoffRate", TypedValueHandler.INT),
            )
        ))

        val catch = field("Catch", ArrayValueHandler(
            ObjectValueHandler(
                field("ErrorEquals", errorListFieldValue),
                assign,
                field("Next", StateReferenceValueHandler()),
                outputN,
                resultPathP
            )
        ))

        val resource = field("Resource", StringValueHandler.INSTANCE)
        val timeout = field("TimeoutSeconds", TypedValueHandler.INT)
        val heartbeat = field("HeartbeatSeconds", TypedValueHandler.INT)
        val timeoutPathP = fieldP("TimeoutSecondsPath", JsonPathLanguageInjector.REFERENCE_PATH)
        val heartbeatPathP = fieldP("HeartbeatSecondsPath", JsonPathLanguageInjector.REFERENCE_PATH)
        val credentials = field("Credentials", TypedValueHandler.OBJECT)

        fun objectHandler(vararg props: Field): ObjectValueHandler {
            return ObjectValueHandler(*props, comment, queryLang, type)
        }

        val stateMachine = ObjectValueHandler(
            field("StartAt", StateReferenceValueHandler()),
            field("States", ParallelStateValueHandler()),
        )

        val choiceRuleJsonata = ObjectValueHandler(
            field("Next", StateReferenceValueHandler()),
            fieldN("Condition", StringValueHandler.INSTANCE),
            outputN, argumentsN,
        )

        states = mapOf(
            Pair("Task", objectHandler(
                outputN, assign, next, end, argumentsN, retry, catch,
                inputDataP, outputDataP, resultPathP, paramP, resultSelectorP, timeoutPathP, heartbeatPathP,
                resource, timeout, heartbeat, credentials
            )),

            Pair("Parallel", objectHandler(outputN, assign, next, end, argumentsN, retry, catch,
                inputDataP, outputDataP, resultPathP, paramP, resultSelectorP,
                field("Branches", ArrayValueHandler(stateMachine))
            )),

            Pair("Map", objectHandler(outputN, assign, next, end, retry, catch,
                inputDataP, outputDataP, resultPathP, paramP, resultSelectorP,
                fieldP("ItemsPath", JsonPathLanguageInjector.REFERENCE_PATH),
                fieldP("MaxConcurrencyPath", JsonPathLanguageInjector.REFERENCE_PATH),
                fieldP("ToleratedFailureCountPath", JsonPathLanguageInjector.REFERENCE_PATH),
                fieldP("ToleratedFailurePercentagePath", JsonPathLanguageInjector.REFERENCE_PATH),

                field("ItemProcessor", stateMachine),
                field("Iterator", stateMachine).skipCompletion(),
                field("Items", JsonPathLanguageInjector.REFERENCE_PATH, TypedValueHandler.ANY), // JSONata - array or string

                field("ItemReader", ObjectValueHandler(
                    field("Resource", StringValueHandler.INSTANCE),
                    argumentsN, paramP,
                    field("ReaderConfig", ObjectValueHandler(
                        field("MaxItems", TypedValueHandler.INT),
                        fieldP("MaxItemsPath", JsonPathLanguageInjector.REFERENCE_PATH),

                        // See https://docs.aws.amazon.com/step-functions/latest/dg/input-output-itemreader.html?utm_source=chatgpt.com
                        field("InputType", StringValueHandler.of("CSV", "JSON", " JSONL", "PARQUET", "MANIFEST")),
                        field("Transformation", StringValueHandler.of("NONE", "LOAD_AND_FLATTEN")),
                        field("ManifestType", StringValueHandler.of("ATHENA_DATA", "S3_INVENTORY")),
                        field("CSVDelimiter", StringValueHandler.of("COMMA", "PIPE", "SEMICOLON", "SPACE", "TAB")),
                        field("CSVHeaderLocation", StringValueHandler.of("FIRST_ROW", "GIVEN")),
                        field("CSVHeaders", TypedValueHandler.ARRAY),
                    ))
                )),

                field("ItemBatcher", ObjectValueHandler(
                    fieldP("MaxItemsPerBatch", TypedValueHandler.INT),
                    fieldP("MaxItemsPerBatchPath", JsonPathLanguageInjector.REFERENCE_PATH),
                    fieldN("MaxItemsPerBatch", TypedValueHandler.INT),

                    field("MaxInputBytesPerBatch", TypedValueHandler.INT),
                    fieldP("MaxInputBytesPerBatchPath", JsonPathLanguageInjector.REFERENCE_PATH),
                )),

                field("ItemSelector", PayloadTemplateValueHandler()),

                field("ResultWriter", ObjectValueHandler(
                    field("Resource", StringValueHandler.INSTANCE),
                    argumentsN, paramP,
                    field("WriterConfig", ObjectValueHandler(
                        field("Transformation", StringValueHandler.INSTANCE),
                        field("OutputType", StringValueHandler.INSTANCE),
                    ))
                )),

                field("MaxConcurrency", TypedValueHandler.INT),
                field("ToleratedFailurePercentage", TypedValueHandler.INT),
                field("ToleratedFailureCount", TypedValueHandler.INT)
            )),

            Pair("Pass", objectHandler(outputN, assign, next, end,
                inputDataP, outputDataP, resultPathP, paramP
                )),

            Pair("Wait", objectHandler(outputN, assign, next, end,
                inputDataP, outputDataP,
                field("Seconds", TypedValueHandler.INT),
                fieldP("SecondsPath", JsonPathLanguageInjector.REFERENCE_PATH),
                field("Timestamp", StringValueHandler.INSTANCE),
                fieldP("TimestampPath", JsonPathLanguageInjector.REFERENCE_PATH),
            )),

            Pair("Choice", objectHandler(outputN, assign,
                inputDataP, outputDataP,
                field("Default", StateReferenceValueHandler()),

                field("Choices", ArrayValueHandler(createChoiceJsonPathHandler()),
                    ArrayValueHandler(choiceRuleJsonata)),
            )),

            Pair("Succeed", objectHandler(outputN, inputDataP, outputDataP)),

            Pair("Fail", objectHandler(
                    field("Error", StringValueHandler.INSTANCE),
                    field("Cause", StringValueHandler.INSTANCE),
                    fieldP("ErrorPath", JsonPathLanguageInjector.REFERENCE_PATH),
                    fieldP("CausePath", JsonPathLanguageInjector.REFERENCE_PATH)
                )
            ),
        )

        assert(stateTypes.toList() == states.keys.toList())

        noTypeObject = objectHandler()
    }

    private fun createChoiceJsonPathHandler(): SchemaValueHandler {
        val conditionRef = DelegateValueHandler()

        val conditionFields = arrayOf(
            field("Variable", JsonPathLanguageInjector.INSTANCE),

            field("StringEquals", StringValueHandler.INSTANCE),
            field("StringEqualsPath", JsonPathLanguageInjector.INSTANCE),
            field("StringLessThan", StringValueHandler.INSTANCE),
            field("StringLessThanPath", JsonPathLanguageInjector.INSTANCE),
            field("StringGreaterThan", StringValueHandler.INSTANCE),
            field("StringGreaterThanPath", JsonPathLanguageInjector.INSTANCE),
            field("StringLessThanEquals", StringValueHandler.INSTANCE),
            field("StringLessThanEqualsPath", JsonPathLanguageInjector.INSTANCE),
            field("StringGreaterThanEquals", StringValueHandler.INSTANCE),
            field("StringGreaterThanEqualsPath", JsonPathLanguageInjector.INSTANCE),

            field("StringMatches", StringValueHandler.INSTANCE),

            field("NumericEquals", TypedValueHandler.INT),
            field("NumericEqualsPath", JsonPathLanguageInjector.INSTANCE),
            field("NumericLessThan", TypedValueHandler.INT),
            field("NumericLessThanPath", JsonPathLanguageInjector.INSTANCE),
            field("NumericGreaterThan", TypedValueHandler.INT),
            field("NumericGreaterThanPath", JsonPathLanguageInjector.INSTANCE),
            field("NumericLessThanEquals", TypedValueHandler.INT),
            field("NumericLessThanEqualsPath", JsonPathLanguageInjector.INSTANCE),
            field("NumericGreaterThanEquals", TypedValueHandler.INT),
            field("NumericGreaterThanEqualsPath", JsonPathLanguageInjector.INSTANCE),
            field("BooleanEquals", TypedValueHandler.BOOL),
            field("BooleanEqualsPath", JsonPathLanguageInjector.INSTANCE),
            field("TimestampEquals", StringValueHandler.INSTANCE),
            field("TimestampEqualsPath", JsonPathLanguageInjector.INSTANCE),
            field("TimestampLessThan", StringValueHandler.INSTANCE),
            field("TimestampLessThanPath", JsonPathLanguageInjector.INSTANCE),
            field("TimestampGreaterThan", StringValueHandler.INSTANCE),
            field("TimestampGreaterThanPath", JsonPathLanguageInjector.INSTANCE),
            field("TimestampLessThanEquals", StringValueHandler.INSTANCE),
            field("TimestampLessThanEqualsPath", JsonPathLanguageInjector.INSTANCE),
            field("TimestampGreaterThanEquals", StringValueHandler.INSTANCE),
            field("TimestampGreaterThanEqualsPath", JsonPathLanguageInjector.INSTANCE),

            field("IsNull", TypedValueHandler.BOOL),
            field("IsPresent", TypedValueHandler.BOOL),
            field("IsNumeric", TypedValueHandler.BOOL),
            field("IsString", TypedValueHandler.BOOL),
            field("IsBoolean", TypedValueHandler.BOOL),
            field("IsTimestamp", TypedValueHandler.BOOL),

            field("Not", conditionRef),
            field("And", ArrayValueHandler(conditionRef)),
            field("Or", ArrayValueHandler(conditionRef)),
        )

        val condition = ObjectValueHandler(*conditionFields)
        conditionRef.delegate = condition

        return ObjectValueHandler(*conditionFields, field("Next", StateReferenceValueHandler()))
    }

    override fun getHandler(language: Lazy<String>, path: PeekableIterator<PsiElement>): SchemaValueHandler? {
        if (!path.hasNext()) return null
        if (path.next() !is JsonObject) return null // states value object

        if (path.next() !is JsonProperty) return null // state definition property

        if (!path.hasNext()) return null
        val obj = path.peek() as? JsonObject ?: return null

        val handler = obj.findProperty("Type")
            ?.let { typeProp -> (typeProp.value as? JsonStringLiteral)?.value?.let { states[it] } }
            ?: noTypeObject

        val language = lazy(LazyThreadSafetyMode.NONE) {
            val res = getLanguage(obj)
            if (res == AslUtils.JSONata) AslUtils.JSONata else AslUtils.JSON_PATH
        }

        return handler.getHandler(language, path)
    }

    private fun getLanguage(state: JsonObject): String? {
        val l = state.findProperty("QueryLanguage")
        if (l != null) return (l.value as? JsonStringLiteral)?.value

        val root = state.parent?.parent?.parent?.parent as? JsonObject ?: return null

        return (root.findProperty("QueryLanguage")?.value as? JsonStringLiteral)?.value
    }

    companion object {

        val INSTANCE = StateDefinitionObjectHandler()

        private fun pathFromStateObj(prop: PsiElement): List<PsiElement>? {
            val res: MutableList<PsiElement> = ArrayList()

            var o: PsiElement = prop

            while (o !is JsonFile) {
                res.add(o)
                o = o.parent
            }

            if (res.removeLastOrNull() !is JsonObject) return null // root object
            val statesProp = res.removeLastOrNull() as? JsonProperty ?: return null

            if (statesProp.name != "States") return null

            res.reverse()

            return res
        }

        private val jsonPathLanguage = lazyOf(AslUtils.JSON_PATH)

        fun getValueHandler(element: PsiElement): SchemaValueHandler? {
            val path = pathFromStateObj(element) ?: return null
            return INSTANCE.getHandler(jsonPathLanguage, PeekableIteratorWrapper(path.iterator()))
        }
    }

}