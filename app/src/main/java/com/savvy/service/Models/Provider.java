package com.savvy.service.Models;

import java.io.Serializable;

public class Provider implements Serializable {
    String image_url, exp, exp_years, verified, id, name, gender_title, gender_id, age, birth_date, phone, city, city_id, country, country_id, main_service, main_service_id, sub_service, sub_service_id, certificates, qualityRate, priceRate, timeRate, behaviorRate, peopleRate, subscription = "";
    String[] areasS, areasId;

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }

    public String getCity_id() {
        return city_id;
    }

    public void setCity_id(String city_id) {
        this.city_id = city_id;
    }

    public String getCountry_id() {
        return country_id;
    }

    public void setCountry_id(String country_id) {
        this.country_id = country_id;
    }

    public String getGender_id() {
        return gender_id;
    }

    public void setGender_id(String gender_id) {
        this.gender_id = gender_id;
    }

    public String getMain_service_id() {
        return main_service_id;
    }

    public void setMain_service_id(String main_service_id) {
        this.main_service_id = main_service_id;
    }

    public String getSub_service_id() {
        return sub_service_id;
    }

    public void setSub_service_id(String sub_service_id) {
        this.sub_service_id = sub_service_id;
    }
    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getExp_years() {
        return exp_years;
    }

    public void setExp_years(String exp_years) {
        this.exp_years = exp_years;
    }

    public String getPeopleRate() {
        return peopleRate;
    }

    public void setPeopleRate(String peopleRate) {
        this.peopleRate = peopleRate;
    }

    public String getPriceRate() {
        return priceRate;
    }

    public void setPriceRate(String priceRate) {
        this.priceRate = priceRate;
    }

    public String getQualityRate() {
        return qualityRate;
    }

    public void setQualityRate(String qualityRate) {
        this.qualityRate = qualityRate;
    }

    public String getBehaviorRate() {
        return behaviorRate;
    }

    public void setBehaviorRate(String behaviorRate) {
        this.behaviorRate = behaviorRate;
    }

    public String getTimeRate() {
        return timeRate;
    }

    public void setTimeRate(String timeRate) {
        this.timeRate = timeRate;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String[] getAreasS() {
        return areasS;
    }

    public void setAreasS(String[] areasS) {
        this.areasS = areasS;
    }

    public String[] getAreasId() {
        return areasId;
    }

    public void setAreasId(String[] areasId) {
        this.areasId = areasId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getCertificates() {
        return certificates;
    }

    public void setCertificates(String certificates) {
        this.certificates = certificates;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGender_title() {
        return gender_title;
    }

    public void setGender_title(String gender_title) {
        this.gender_title = gender_title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMain_service() {
        return main_service;
    }

    public void setMain_service(String main_service) {
        this.main_service = main_service;
    }

    public String getSub_service() {
        return sub_service;
    }

    public void setSub_service(String sub_service) {
        this.sub_service = sub_service;
    }
}
