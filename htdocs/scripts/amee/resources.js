function alertModal(message) {
    new ModalHelper({
        owner: this,
        title: 'Alert',
        message: message,
        okText: 'Ok'
    });
}

var DeleteResource = Class.create();
DeleteResource.prototype = {
    initialize: function() {
        this.action = "delete";
        this.resourceUrl = '';
        this.resourceElem = null;
        this.resourceType = '';
        this.modal = null;
        this.confirm = true;
    },
    deleteResource: function(resourceUrl, resourceElem, resourceType) {
        this.resourceUrl = resourceUrl;
        this.resourceElem = resourceElem;
        this.resourceType = resourceType;
        if (this.confirm) {
            this.showModal();
        } else {
            this.ok();
        }
    },
    showModal: function() {
        new ModalHelper({
            owner: this,
            title: 'Warning',
            message: "Are you sure you want to delete this " + this.resourceType + "?",
            okText: 'Yes',
            cancelText: 'No',
            onOk: this.ok.bind(this)});
        return true;
    },
    ok: function(win) {
        if (this.modal != null) {
            this.modal.close();
            this.modal = null;
        }
        var myAjax = new Ajax.Request(
                this.resourceUrl, {
            method: 'post',
            parameters: 'method=delete',
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.success.bind(this),
            onFailure: this.failure.bind(this)});
        return true;
    },
    success: function(response) {
        this.successCallback(response);
    },
    successCallback: function(response) {
        window.location = window.location.href;
    },
    failure: function(response) {
        this.failureCallback(response);
    },
    failureCallback: function(response) {
        alertModal("This " + this.resourceType + " cannot be deleted.");
    }
};

var CreateResource = Class.create();
CreateResource.prototype = {
    initialize: function() {
        this.action = "create";
        this.resourceUrl = '';
        this.resourceElem = null;
        this.resourceType = '';
        this.params = '';
        this.modal = null;
        this.confirm = true;
    },
    createResource: function(resourceUrl, resourceElem, resourceType, params) {
        this.resourceUrl = resourceUrl;
        this.resourceElem = resourceElem;
        this.resourceType = resourceType;
        this.params = params;
        if (this.confirm) {
            this.modal = new Control.Modal(false, {
                contents: createModalContents(
                        "Are you sure you want to add this " + this.resourceType + "?",
                        "Yes",
                        "No"),
                width: 400,
                height: 200,
                afterOpen: this.afterOpen.bind(this)
            });
            this.modal.resource = this;
            this.modal.open();
        } else {
            this.ok();
        }
    },
    afterOpen: function() {
        Event.observe('okButton', "click", this.ok.bindAsEventListener(this));
        Event.observe('cancelButton', "click", this.cancel.bindAsEventListener(this));
    },
    ok: function() {
        if (this.modal != null) {
            this.modal.close();
            this.modal = null;
        }
        var myAjax = new Ajax.Request(
                this.resourceUrl, {
            method: 'post',
            parameters: this.params,
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: this.success.bind(this)});
        return true;
    },
    cancel: function() {
        if (this.modal != null) {
            this.modal.close();
            this.modal = null;
        }
    },
    success: function(response) {
        this.successCallback(response);
    },
    successCallback: function() {
        Effect.Highlight(this.resourceElem);
    }
};