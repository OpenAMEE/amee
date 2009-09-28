/**
 * ModalHelper class. Helper for creating modal windows.
 */
var ModalHelper = Class.create({
    initialize: function(params) {
        params = params || {};
        this.owner = params.owner || null;
        this.element = params.element || null;
        this.title = params.title || 'Attention';
        this.message = params.message || 'Are you sure?';
        this.okText = params.okText || 'Yes';
        this.cancelText = params.cancelText || 'No';
        this.form = params.form || null;
        this.onOk = params.onOk || null;
        this.onCancel = params.onCancel || null;
        this.showCancel = (params.showCancel !== undefined) ? params.showCancel : true;
        this.modal = this.getModal();
        this.modal.open();
    },
    getModal: function() {
        if (!this.modal) {
            this.modal = new Control.Modal(false, {
                width: 420,
                height: 120,
                afterOpen: this.afterOpen.bind(this)
            });
            this.modal.container.insert(this.getModalElement());
        }
        return this.modal;
    },
    afterOpen: function() {
    },
    ok: function(event) {
        event.stop();
        this.modal.close();
        this.modal = null;
        if (this.onOk) {
            this.onOk(this);
        }
        return false;
    },
    cancel: function(event) {
        event.stop();
        this.modal.close();
        this.modal = null;
        if (this.onCancel) {
            this.onCancel(this);
        }
        return false;
    },
    getModalElement: function() {
        var element = new Element('div').addClassName("loginModalHead clearfix");
        var outerDiv = new Element('div').addClassName("loginOuterDiv")
                .insert(new Element('h2').update(this.title))
                .insert(new Element('div').addClassName("loginInnerDiv clearfix").update(this.message));
        if (this.form) {
            outerDiv.insert(this.form);
        }
        var buttonsOuter = new Element('div').addClassName("loginButtonsOuter clearfix");
        var buttonsInner = new Element('div');
        var okButton = new Element('button').addClassName("button").update(this.okText);
        okButton.observe("click", this.ok.bindAsEventListener(this));
        if (this.showCancel) {
            var cancelButton = new Element('button').addClassName("cancelButton").update(this.cancelText);
            cancelButton.observe("click", this.cancel.bindAsEventListener(this));
            buttonsInner.insert(cancelButton);
        }
        buttonsInner.insert(okButton);
        buttonsOuter.insert(buttonsInner);
        outerDiv.insert(buttonsOuter);
        element.insert(outerDiv);
        return element;
    }
});

function alertModal(message) {
    new ModalHelper({
        owner: this,
        title: 'Alert',
        message: message,
        okText: 'Ok'
    });
}

var DeleteResource = Class.create({
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
        new Ajax.Request(this.resourceUrl, {
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
});

var CreateResource = Class.create({
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
        new Ajax.Request(
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
});
