# == Schema Information
#
# Table name: accounts
#
#  id                                        :integer          not null, primary key
#  created_at                                :datetime
#  updated_at                                :datetime
#  sf_id                                     :string(255)
#  FirstName                                 :string(255)
#  LastName                                  :string(255)
#  SS_Num__c                                 :string(255)
#  Phone                                     :string(255)
#  PersonEmail                               :string(255)
#  Gender__c                                 :string(255)
#  Identify_as_GLBT__c                       :boolean
#  Race__c                                   :string(255)
#  Primary_Language__c                       :string(255)
#  Foster_Care__c                            :boolean
#  Veteran__c                                :boolean
#  Housing_Status_New__c                     :string(255)
#  How_long_have_you_been_homeless__c        :string(255)
#  Where_do_you_usually_go_for_healthcare__c :string(255)
#  Medical_Care_Other__c                     :string(255)
#  Birthdate__c                              :date
#

require 'faker'

FactoryGirl.define do
  factory :account do |f|
    f.FirstName { Faker::Name.first_name }
    f.LastName { Faker::Name.last_name }
    f.Birthdate__c { Faker::Date.between(100.years.ago, Date.today) }
    f.PersonEmail { Faker::Internet.email }
    f.sf_id { rand(36**16).to_s(36) }
  end

end
