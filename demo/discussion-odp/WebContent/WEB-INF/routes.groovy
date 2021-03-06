println ("building routing...")

router.GET('topics') {
	strategy(SELECT_ALL_DOCUMENTS_BY_VIEW) {
		viewName('($All)')
	}
	mapJson 'id', json:'id', type:'STRING', isformula:true, formula:'@DocumentUniqueID'
	mapJson "date", json:'date',type:'DATETIME',isformula:true, formula:'@Created'
	mapJson "Subject", json:'topic', type:'STRING'
	mapJson "author", json:'author', type:'STRING',isformula:true,formula:'@Name([CN]; From)'
}
router.GET('topics/{id}') {
	strategy(SELECT_DOCUMENT_BY_UNID) {
			keyVariableName("id")
	}
	mapJson "date", json:'date',type:'STRING',isformula:true, formula:'@Text(@Created)'
	mapJson "Subject", json:'topic', type:'STRING'
	mapJson "author", json:'author', type:'STRING',isformula:true, formula:'@Name([CN]; From)'
	mapJson "body", json:'content', type:'MIME'
	mapJson "categories", json:'categories', type:'ARRAY_OF_STRING'
}
router.GET('topics/{id}/attachment/{attachmentName}') {
	strategy(SELECT_ATTACHMENT) {
		documentStrategy(SELECT_DOCUMENT_BY_UNID) {
			keyVariableName("id")
		}
		fieldName "Body"
		selectionType BY_NAME //Could be BY_NAME, FIRST
		attachmentNameVariableName "{attachmentName}"
	}
}
router.POST('topics/{id}') {
	strategy(SELECT_DOCUMENT_BY_UNID) {
		keyVariableName("id")
	}
	mapJson "Subject", json:'topic', type:'STRING'
	mapJson "body", json:'content', type:'MIME'
	mapJson 'categories', json:'categories', type:'ARRAY_OF_STRING', readonly:true
	mapJson 'NewCats', json:'categories', type:'ARRAY_OF_STRING', writeonly:true
	mapJson "date", json:'date',type:'STRING',isformula:true, formula:'@Text(@Created)', readonly:true
	mapJson "author", json:'author', type:'STRING',isformula:true, formula:'@Name([CN]; From)', readonly:true
	
	events PRE_SAVE_DOCUMENT: {
		context, document ->
		nsfHelp = context.getNSFHelper()
		nsfHelp.computeWithForm(document)
	}
}

router.POST('topics/{id}/attachment') {
	strategy(SELECT_ATTACHMENT){
		documentStrategy(SELECT_DOCUMENT_BY_UNID) {
			keyVariableName("id")
		}
		fieldName "Body"
		updateType REPLACE_BY_NAME //Could be REPLACE_ALL, REPLACE_BY_NAME
	}
}

router.GET('topics/{parent_id}/comments') {
	strategy(SELECT_ALL_DOCUMENTS_FROM_VIEW_BY_KEY) {
			keyVariableName("parent_id")
			viewName("commentsByParentId")
	}
	mapJson "date", json:'date',type:'STRING',isformula:true, formula:'@Created'
	mapJson "Subject", json:'topic', type:'STRING'
	mapJson "author", json:'author', type:'STRING',isformula:true,formula:'@Name([CN]; From)'
	mapJson "body", json:'content', type:'MIME'
	mapJson "categories", json:'categories', type:'ARRAY_OF_STRING'
}

router.GET('topics/{parent_id}/comments/{id}') {
	strategy(SELECT_DOCUMENT_BY_UNID) {
			keyVariableName("id")
	}
	mapJson "date", json:'date',type:'STRING',isformula:true, formula:'@Created'
	mapJson "Subject", json:'topic', type:'STRING'
	mapJson "author", json:'author', type:'STRING',isformula:true,formula:'@Name([CN]; From)'
	mapJson "body", json:'content', type:'MIME'
	mapJson "categories", json:'categories', type:'ARRAY_OF_STRING'
}

router.POST('topics/{parent_id}/comments/{id}') {
	strategy(SELECT_DOCUMENT_BY_UNID) {
		keyVariableName("id")
		form 'Response'
	}
	mapJson "Subject", json:'topic', type:'STRING'
	mapJson "body", json:'content', type:'MIME'
	mapJson "categories", json:'categories', type:'ARRAY_OF_STRING'
	mapJson "date", json:'date',type:'STRING',isformula:true, formula:'@Text(@Created)', readonly:true
	mapJson "author", json:'author', type:'STRING',isformula:true, formula:'@Name([CN]; From)', readonly:true
	
	events PRE_SAVE_DOCUMENT:{
		context, document ->
		parentId = context.getRouterVariables().get('parent_id')
		nsfHelp = context.getNSFHelper()
		nsfHelp.makeDocumentAsChild(parentId, document)
		nsfHelp.computeWithForm(document)
	}
}

router.DELETE('document/{id}') {
	strategy(SELECT_DOCUMENT_BY_UNID) {
		keyVariableName("{id}")
	}
}