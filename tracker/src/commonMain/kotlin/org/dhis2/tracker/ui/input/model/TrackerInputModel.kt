
@Composable
private fun SearchOperator.supportingTextString() =
    when (this) {
        SearchOperator.EQ ->
            stringResource(Res.string.equal_search_operator)
        SearchOperator.SW ->
            stringResource(Res.string.starts_with_search_operator)
        SearchOperator.EW ->
            stringResource(Res.string.end_with_search_operator)
        else -> null
    }
