package com.abdalh.microtaxi.model;

public class Driver {

    String  id ,name ,email,phone,carType,password;

        public Driver()
        {

        }

        public Driver(String name, String email, String phone, String carType, String password) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.carType = carType;
            this.password = password;
        }

        public Driver(String id, String name, String email, String phone, String carType, String password) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.carType = carType;
            this.password = password;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getCarType() {
            return carType;
        }

        public void setCarType(String carType) {
            this.carType = carType;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }


