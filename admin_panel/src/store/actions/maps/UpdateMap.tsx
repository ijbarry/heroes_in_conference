import {ConferenceMap} from "../../../maps/ConferenceMap";
import {AppDispatch} from "../../appStore";
import {API} from 'src/api/API';
import {updateCachedMap} from "./UpdateCachedMap";


export async function updateMap(map: ConferenceMap, image: string | undefined, dispatch: AppDispatch) /* : ConferenceMap, but issue if we say this */ {

    let imageData;
    if(image) {
        imageData = await toDataUrl(image);
    }

    const result = await API.updateMap(map, imageData);
    dispatch(updateCachedMap(result));

    return result;
}

function toDataUrl(url : string): Promise<string> {
   const image = new Image();

   const promise = new Promise<string>((resolve, reject) => {
        image.onload = () => {
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');

            if(!ctx) {
                reject("No canvas context support");
                return;
            }

            canvas.height = image.naturalHeight;
            canvas.width = image.naturalWidth;
            ctx.drawImage(image, 0, 0);
            const dataURL = canvas.toDataURL('image/jpg', 0.95);
            resolve(dataURL);
        };
   });

   image.src = url;

   return promise;
}