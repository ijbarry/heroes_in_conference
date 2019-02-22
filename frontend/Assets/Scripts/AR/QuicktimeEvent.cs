using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class QuicktimeEvent : MonoBehaviour
{
    public GameObject sphere;
    public Material material;

    private Renderer renderer;

    float probability;
    int reactionFrames;


    void Start()
    {
        renderer = sphere.GetComponent<MeshRenderer>();
        sphere.SetActive(false);
	// Change this value to make sphere appear more often
        probability = 0.3f;
        reactionFrames = 0;
    }

    void Update()
    {
	if (reactionFrames > 0)
        {
            reactionFrames--;
            if (Input.touchCount > 0 && Input.GetTouch(0).phase == TouchPhase.Began)
            {
                Ray ray = Camera.main.ScreenPointToRay(Input.GetTouch(0).position);

                if (Physics.Raycast(ray, out RaycastHit hit))
                {
                    renderer.material = material;
                }
            }
        }
        else
        {
		sphere.SetActive(false);
		float rand = Random.value;
		print(rand);
            if (rand < probability)
            {
		// Show sphere for 1 second at a time
                reactionFrames = (int) (1.0f / Time.deltaTime);
		sphere.SetActive(true);
	    }
        }
    }
}
