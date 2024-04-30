#include <stdio.h>

int main() {
    bool b;

    switch (2 == 1) {
        case 1:
            int num1 = 10.0;
            int num2 = 20;
            char c = 'a';
            int sum = num1 + num2;
            bool x = !b;

            printf("Sum of %d and %d is %d\n", num1, num2, sum);

            std::cout << "Hi";

            break;
        default:
            // default case
            break;
    }

    return 0;
}
