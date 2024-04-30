#include <iostream>

namespace Math {
    int add(int a, int b) {
        return a + b;
    }

    namespace Geometry {
        const double PI = 3.14159;

        double circleArea(double radius) {
            return PI * radius * radius;
        }
    }
}

int main() {
    int result = Math::add(3, 4);
    std::cout << "Result of addition: " << result << std::endl;

    double radius = 2.5;
    double area = Math::Geometry::circleArea(radius);
    std::cout << "Area of circle with radius " << radius << ": " << area << std::endl;

    return 0;
}
