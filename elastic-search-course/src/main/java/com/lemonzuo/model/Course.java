package com.lemonzuo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LemonZuo
 * @create 2021-04-08 22:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    private String imgUrl;
    private String courseName;
    private String courseDescription;
    private String studentsCount;
}
