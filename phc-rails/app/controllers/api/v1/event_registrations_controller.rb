class Api::V1::EventRegistrationsController < ApplicationController
  skip_before_action :verify_authenticity_token
  before_action :verify_security_token

  def create
    event_reg = EventRegistration.new

    # Create account if salesforce id was not passed in
    sf_id = params[:account_sfid]
    if sf_id.nil?
      # Create account of the parameters passed in, sf_id will be nil
      params[:sf_id] = params.delete :account_sfid
      account = Account.spawn(params)

      # Since we don't know the SF id yet, use the unique rails id for now to identify the account
      event_reg[:account_sfid] = account.id
    else
      # We retrieve the corresponding account if the salesforce id was passed in
      if not Account.exists?(sf_id: sf_id)
        raise "Unknown Salesforce ID. This should not happen!"
      end
      account = Account.find_by(sf_id: params[:account_sfid])
      event_reg[:account_sfid] = account.sf_id
    end

    # Number__c is the QR_code that was passed in, which identifies a
    # particular account for the current PHC event
    event_reg.Number__c = params[:Number__c]
    Service.services.each do |service|
      status = (if params[service] == true then Service.APPLIED else Service.NONE end)
      (event_reg.services ||= []) << Service.new(name: service, status:status)
    end

    status = event_reg.save ? "Success" : "Failure"
    api_message_response(status)
  end

  def search
    qr_code = request.headers["HTTP_NUMBER__C"]
    puts qr_code
    render json: { present: (EventRegistration.exists?(Number__c: qr_code) ? true : false) }
  end

  def get_applied
    @event_registration = EventRegistration.find_by(Number__c: params[:Number__c])
    if @event_registration.exists?
      @services = @event_registration.services
      render json: { status: "true", services: ["Acupuncture", "Haircuts", "Massage"] }
    else
      api_message_response(404, "Event registration with that number does not exist.")
    end
  end

  def update_service
    registration = EventRegistration.find_by(Number__c: params[:Number__c])
    if registration.nil?
      api_message_response(404, "Event registration with that number does not exist.")
      return
    end

    service = registration.services.find_by(name: params[:service_name])
    if service.nil?
      api_message_response(404, "Service with that name does not exist.")
      return
    end

    case service.status
    when Service.NONE
      service.update_attribute(:status, Service.DROPIN)
      render json: { message: "The client is a drop-in." }
      return
    when Service.APPLIED
      service.update_attribute(:status, Service.RECIEVED)
      render json: { message: "" }
      return
    when Service.RECIEVED
      render json: { message: "The client has recieved this service before." }
      return
    when Service.DROPIN
      render json: { message: "The client has recieved this service before." }
      return
    else
      raise "Service status is not known. This should not happen!"
    end
  end

  def update_feedback
    @event_registration = EventRegistration.find_by(Number__c: params[:Number__c])
    if @event_registration.update(event_registration_params)
      api_message_response(201, "Good job Shimmy")
    else
      api_message_response(404, "Event registration with that number does not exist.")
    end
  end

  private

  def event_registration_params
    params.permit(:Number__c, :Experience__c, :Services_Needed__c, :Feedback__c)
  end

end
