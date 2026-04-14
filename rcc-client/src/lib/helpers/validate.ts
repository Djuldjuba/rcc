import {Errors} from "$lib/types/Errors";

const MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

export const validateImage = (file: File): string => {
    if (!ALLOWED_TYPES.includes(file.type)) {
        return Errors.IMAGE_INVALID_TYPE;
    }
    if (file.size > MAX_IMAGE_SIZE) {
        return Errors.IMAGE_CONSTRAINT_TOO_BIG;
    }
    return "";
};